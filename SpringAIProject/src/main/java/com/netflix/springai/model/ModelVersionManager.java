package com.netflix.springai.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

@Service
public class ModelVersionManager {

    private static final Logger logger = LoggerFactory.getLogger(ModelVersionManager.class);

    private final ModelVersionRepository modelVersionRepository;
    private final ModelPerformanceRepository modelPerformanceRepository;
    private final CacheManager cacheManager;

    // In-memory cache for active model versions
    private final Map<String, AIModelVersion> activeModelVersions = new ConcurrentHashMap<>();
    private final ReentrantReadWriteLock modelLock = new ReentrantReadWriteLock();

    @Autowired
    public ModelVersionManager(
            ModelVersionRepository modelVersionRepository,
            ModelPerformanceRepository modelPerformanceRepository,
            CacheManager cacheManager) {
        this.modelVersionRepository = modelVersionRepository;
        this.modelPerformanceRepository = modelPerformanceRepository;
        this.cacheManager = cacheManager;

        // Initialize active models
        loadActiveModelVersions();
    }

    @Transactional(readOnly = true)
    public List<AIModelVersion> getActiveModelVersions() {
        modelLock.readLock().lock();
        try {
            return new ArrayList<>(activeModelVersions.values());
        } finally {
            modelLock.readLock().unlock();
        }
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "modelVersions", key = "#modelType")
    public Optional<AIModelVersion> getActiveModelVersion(String modelType) {
        modelLock.readLock().lock();
        try {
            return Optional.ofNullable(activeModelVersions.get(modelType));
        } finally {
            modelLock.readLock().unlock();
        }
    }

    @Transactional
    public AIModelVersion createNewModelVersion(AIModelVersion modelVersion) {
        logger.info("Creating new model version: {} v{}", modelVersion.getModelType(), modelVersion.getVersion());

        // Validate version doesn't already exist
        if (modelVersionRepository.existsByModelTypeAndVersion(modelVersion.getModelType(), modelVersion.getVersion())) {
            throw new IllegalArgumentException(
                String.format("Model version %s v%s already exists", modelVersion.getModelType(), modelVersion.getVersion())
            );
        }

        // Set creation metadata
        modelVersion.setCreatedAt(LocalDateTime.now());
        modelVersion.setStatus(ModelStatus.DRAFT);

        AIModelVersion savedVersion = modelVersionRepository.save(modelVersion);
        logger.info("Created model version with ID: {}", savedVersion.getId());

        return savedVersion;
    }

    @Transactional
    public AIModelVersion activateModelVersion(String modelType, String version) {
        logger.info("Activating model version: {} v{}", modelType, version);

        AIModelVersion modelVersion = modelVersionRepository.findByModelTypeAndVersion(modelType, version)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Model version %s v%s not found", modelType, version)
            ));

        // Validate model can be activated
        validateModelForActivation(modelVersion);

        modelLock.writeLock().lock();
        try {
            // Deactivate current active version
            AIModelVersion currentActive = activeModelVersions.get(modelType);
            if (currentActive != null) {
                deactivateModelVersion(currentActive);
            }

            // Activate new version
            modelVersion.setStatus(ModelStatus.ACTIVE);
            modelVersion.setActivatedAt(LocalDateTime.now());
            AIModelVersion activatedVersion = modelVersionRepository.save(modelVersion);

            // Update in-memory cache
            activeModelVersions.put(modelType, activatedVersion);

            // Clear cache
            evictModelCache(modelType);

            logger.info("Activated model version: {} v{}", modelType, version);
            return activatedVersion;

        } finally {
            modelLock.writeLock().unlock();
        }
    }

    @Transactional
    public AIModelVersion deactivateModelVersion(String modelType, String version) {
        logger.info("Deactivating model version: {} v{}", modelType, version);

        AIModelVersion modelVersion = modelVersionRepository.findByModelTypeAndVersion(modelType, version)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Model version %s v%s not found", modelType, version)
            ));

        return deactivateModelVersion(modelVersion);
    }

    private AIModelVersion deactivateModelVersion(AIModelVersion modelVersion) {
        modelVersion.setStatus(ModelStatus.INACTIVE);
        modelVersion.setDeactivatedAt(LocalDateTime.now());

        AIModelVersion savedVersion = modelVersionRepository.save(modelVersion);

        // Remove from active versions if present
        activeModelVersions.remove(modelVersion.getModelType());

        // Clear cache
        evictModelCache(modelVersion.getModelType());

        logger.info("Deactivated model version: {} v{}", modelVersion.getModelType(), modelVersion.getVersion());
        return savedVersion;
    }

    @Transactional
    public AIModelVersion archiveModelVersion(String modelType, String version) {
        logger.info("Archiving model version: {} v{}", modelType, version);

        AIModelVersion modelVersion = modelVersionRepository.findByModelTypeAndVersion(modelType, version)
            .orElseThrow(() -> new IllegalArgumentException(
                String.format("Model version %s v%s not found", modelType, version)
            ));

        if (modelVersion.getStatus() == ModelStatus.ACTIVE) {
            throw new IllegalStateException("Cannot archive active model version");
        }

        modelVersion.setStatus(ModelStatus.ARCHIVED);
        modelVersion.setArchivedAt(LocalDateTime.now());

        AIModelVersion archivedVersion = modelVersionRepository.save(modelVersion);

        // Clear cache
        evictModelCache(modelType);

        logger.info("Archived model version: {} v{}", modelType, version);
        return archivedVersion;
    }

    @Transactional(readOnly = true)
    public Page<AIModelVersion> getModelVersions(String modelType, Pageable pageable) {
        return modelVersionRepository.findByModelTypeOrderByCreatedAtDesc(modelType, pageable);
    }

    @Transactional(readOnly = true)
    public List<AIModelVersion> getModelVersionsByStatus(ModelStatus status) {
        return modelVersionRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Optional<AIModelVersion> getModelVersionById(Long id) {
        return modelVersionRepository.findById(id);
    }

    @Transactional
    public AIModelVersion updateModelMetadata(Long modelVersionId, Map<String, Object> metadata) {
        AIModelVersion modelVersion = modelVersionRepository.findById(modelVersionId)
            .orElseThrow(() -> new IllegalArgumentException("Model version not found"));

        modelVersion.setMetadata(metadata);
        modelVersion.setUpdatedAt(LocalDateTime.now());

        return modelVersionRepository.save(modelVersion);
    }

    @Transactional
    public void recordModelPerformance(String modelType, String version, ModelPerformanceMetrics metrics) {
        AIModelVersion modelVersion = modelVersionRepository.findByModelTypeAndVersion(modelType, version)
            .orElseThrow(() -> new IllegalArgumentException("Model version not found"));

        ModelPerformance performance = new ModelPerformance();
        performance.setModelVersion(modelVersion);
        performance.setRecordedAt(LocalDateTime.now());
        performance.setResponseTime(metrics.getAverageResponseTime());
        performance.setSuccessRate(metrics.getSuccessRate());
        performance.setTokenUsage(metrics.getTokenUsage());
        performance.setCost(metrics.getCost());
        performance.setErrorRate(metrics.getErrorRate());
        performance.setMetadata(metrics.getMetadata());

        modelPerformanceRepository.save(performance);

        // Update model version performance summary
        updateModelPerformanceSummary(modelVersion, metrics);
    }

    private void updateModelPerformanceSummary(AIModelVersion modelVersion, ModelPerformanceMetrics metrics) {
        // Calculate rolling averages (last 100 requests)
        List<ModelPerformance> recentPerformance = modelPerformanceRepository
            .findTop100ByModelVersionOrderByRecordedAtDesc(modelVersion);

        if (!recentPerformance.isEmpty()) {
            double avgResponseTime = recentPerformance.stream()
                .mapToDouble(ModelPerformance::getResponseTime)
                .average().orElse(0.0);

            double avgSuccessRate = recentPerformance.stream()
                .mapToDouble(ModelPerformance::getSuccessRate)
                .average().orElse(0.0);

            modelVersion.setAverageResponseTime(avgResponseTime);
            modelVersion.setAverageSuccessRate(avgSuccessRate);
            modelVersion.setLastPerformanceUpdate(LocalDateTime.now());

            modelVersionRepository.save(modelVersion);
        }
    }

    @Transactional(readOnly = true)
    public List<ModelPerformance> getModelPerformanceHistory(String modelType, String version, LocalDateTime since) {
        AIModelVersion modelVersion = modelVersionRepository.findByModelTypeAndVersion(modelType, version)
            .orElseThrow(() -> new IllegalArgumentException("Model version not found"));

        return modelPerformanceRepository.findByModelVersionAndRecordedAtAfterOrderByRecordedAtDesc(modelVersion, since);
    }

    public boolean shouldPromoteModel(String modelType, String version, ModelPerformanceMetrics currentMetrics) {
        // Auto-promotion logic based on performance thresholds
        AIModelVersion currentActive = getActiveModelVersion(modelType).orElse(null);
        if (currentActive == null) return true;

        // Promote if new version has significantly better performance
        double improvementThreshold = 0.1; // 10% improvement

        return currentMetrics.getSuccessRate() > currentActive.getAverageSuccessRate() + improvementThreshold &&
               currentMetrics.getAverageResponseTime() < currentActive.getAverageResponseTime() * (1 - improvementThreshold);
    }

    public Map<String, Object> getModelHealthStatus(String modelType) {
        Map<String, Object> health = new HashMap<>();

        Optional<AIModelVersion> activeVersion = getActiveModelVersion(modelType);
        if (activeVersion.isPresent()) {
            AIModelVersion version = activeVersion.get();
            health.put("status", "HEALTHY");
            health.put("activeVersion", version.getVersion());
            health.put("averageResponseTime", version.getAverageResponseTime());
            health.put("averageSuccessRate", version.getAverageSuccessRate());
            health.put("lastPerformanceUpdate", version.getLastPerformanceUpdate());
        } else {
            health.put("status", "NO_ACTIVE_MODEL");
            health.put("message", "No active model version found for type: " + modelType);
        }

        return health;
    }

    private void validateModelForActivation(AIModelVersion modelVersion) {
        if (modelVersion.getStatus() == ModelStatus.ARCHIVED) {
            throw new IllegalStateException("Cannot activate archived model version");
        }

        // Check minimum performance requirements
        if (modelVersion.getAverageSuccessRate() != null && modelVersion.getAverageSuccessRate() < 0.95) {
            throw new IllegalStateException("Model version must have at least 95% success rate to be activated");
        }

        // Check if model has been tested
        if (!modelVersion.isTested()) {
            throw new IllegalStateException("Model version must be tested before activation");
        }
    }

    @CacheEvict(value = "modelVersions", key = "#modelType")
    public void evictModelCache(String modelType) {
        // Cache eviction is handled by annotation
    }

    private void loadActiveModelVersions() {
        List<AIModelVersion> activeVersions = modelVersionRepository.findByStatus(ModelStatus.ACTIVE);

        modelLock.writeLock().lock();
        try {
            activeModelVersions.clear();
            for (AIModelVersion version : activeVersions) {
                activeModelVersions.put(version.getModelType(), version);
            }
        } finally {
            modelLock.writeLock().unlock();
        }

        logger.info("Loaded {} active model versions", activeModelVersions.size());
    }
}
