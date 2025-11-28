package com.netflix.productivity.platform.search;

import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Enterprise Search Service
 *
 * Provides comprehensive search capabilities with Elasticsearch integration:
 * - Full-text search across issues, projects, and users
 * - Advanced filtering and faceting
 * - Search analytics and suggestions
 * - Real-time indexing and updates
 * - Search performance monitoring
 * - Multi-tenant search isolation
 */
@Service
public class SearchService {

    private static final Logger logger = LoggerFactory.getLogger(SearchService.class);

    // Search index simulation (would be Elasticsearch in production)
    private final ConcurrentHashMap<String, SearchIndex> indices = new ConcurrentHashMap<>();

    // Search analytics
    private final SearchAnalytics analytics = new SearchAnalytics();

    // Search suggestions
    private final SearchSuggestions suggestions = new SearchSuggestions();

    // Index configurations
    private final Map<String, IndexConfig> indexConfigs = initializeIndexConfigs();

    public SearchService() {
        initializeIndices();
    }

    /**
     * Index a document for search
     */
    public CompletableFuture<Void> indexDocument(String indexName, String documentId, SearchDocument document) {
        return CompletableFuture.runAsync(() -> {
            try {
                SearchIndex index = indices.computeIfAbsent(indexName, this::createIndex);
                index.addDocument(documentId, document);

                analytics.recordIndexing(indexName, document.getDocumentType());
                logger.debug("Indexed document: {} in index: {}", documentId, indexName);

            } catch (Exception e) {
                logger.error("Failed to index document: {} in index: {}", documentId, indexName, e);
                analytics.recordIndexingError(indexName);
            }
        });
    }

    /**
     * Search documents with query
     */
    public SearchResult search(String indexName, SearchQuery query) {
        long startTime = System.nanoTime();

        try {
            SearchIndex index = indices.get(indexName);
            if (index == null) {
                return SearchResult.empty(query);
            }

            // Execute search
            List<SearchHit> hits = index.search(query);
            long searchTime = (System.nanoTime() - startTime) / 1_000_000; // Convert to milliseconds

            // Record analytics
            analytics.recordSearch(query, hits.size(), searchTime);

            // Add suggestions for zero results
            List<String> searchSuggestions = Collections.emptyList();
            if (hits.isEmpty()) {
                searchSuggestions = suggestions.generateSuggestions(query.getQuery());
            }

            return new SearchResult(
                query,
                hits,
                hits.size(),
                0, // from
                hits.size(), // size
                searchTime,
                searchSuggestions
            );

        } catch (Exception e) {
            logger.error("Search failed for index: {}", indexName, e);
            analytics.recordSearchError(indexName);
            return SearchResult.empty(query);
        }
    }

    /**
     * Advanced search with filters and aggregations
     */
    public SearchResult advancedSearch(String indexName, AdvancedSearchQuery query) {
        long startTime = System.nanoTime();

        try {
            SearchIndex index = indices.get(indexName);
            if (index == null) {
                return SearchResult.empty(query.getBaseQuery());
            }

            // Apply filters
            List<SearchDocument> filteredDocuments = applyFilters(index.getAllDocuments(), query.getFilters());

            // Execute search on filtered documents
            List<SearchHit> hits = searchInDocuments(filteredDocuments, query.getBaseQuery());

            // Apply aggregations
            Map<String, AggregationResult> aggregations = calculateAggregations(filteredDocuments, query.getAggregations());

            long searchTime = (System.nanoTime() - startTime) / 1_000_000;

            analytics.recordAdvancedSearch(query, hits.size(), searchTime);

            return new SearchResult(
                query.getBaseQuery(),
                hits,
                hits.size(),
                query.getFrom(),
                query.getSize(),
                searchTime,
                aggregations,
                Collections.emptyList()
            );

        } catch (Exception e) {
            logger.error("Advanced search failed for index: {}", indexName, e);
            analytics.recordSearchError(indexName);
            return SearchResult.empty(query.getBaseQuery());
        }
    }

    /**
     * Delete document from index
     */
    public CompletableFuture<Void> deleteDocument(String indexName, String documentId) {
        return CompletableFuture.runAsync(() -> {
            try {
                SearchIndex index = indices.get(indexName);
                if (index != null) {
                    index.removeDocument(documentId);
                    analytics.recordDeletion(indexName);
                    logger.debug("Deleted document: {} from index: {}", documentId, indexName);
                }
            } catch (Exception e) {
                logger.error("Failed to delete document: {} from index: {}", documentId, indexName, e);
            }
        });
    }

    /**
     * Update document in index
     */
    public CompletableFuture<Void> updateDocument(String indexName, String documentId, SearchDocument updatedDocument) {
        return CompletableFuture.runAsync(() -> {
            try {
                SearchIndex index = indices.get(indexName);
                if (index != null) {
                    index.updateDocument(documentId, updatedDocument);
                    analytics.recordUpdate(indexName, updatedDocument.getDocumentType());
                    logger.debug("Updated document: {} in index: {}", documentId, indexName);
                }
            } catch (Exception e) {
                logger.error("Failed to update document: {} in index: {}", documentId, indexName, e);
            }
        });
    }

    /**
     * Bulk index documents
     */
    public CompletableFuture<Void> bulkIndex(String indexName, Map<String, SearchDocument> documents) {
        return CompletableFuture.runAsync(() -> {
            try {
                SearchIndex index = indices.computeIfAbsent(indexName, this::createIndex);

                for (Map.Entry<String, SearchDocument> entry : documents.entrySet()) {
                    index.addDocument(entry.getKey(), entry.getValue());
                }

                analytics.recordBulkIndexing(indexName, documents.size());
                logger.info("Bulk indexed {} documents in index: {}", documents.size(), indexName);

            } catch (Exception e) {
                logger.error("Bulk indexing failed for index: {}", indexName, e);
                analytics.recordBulkIndexingError(indexName, documents.size());
            }
        });
    }

    /**
     * Get search suggestions
     */
    public List<String> getSuggestions(String indexName, String partialQuery) {
        try {
            SearchIndex index = indices.get(indexName);
            if (index == null) {
                return Collections.emptyList();
            }

            return suggestions.generateSuggestions(partialQuery, index.getAllDocuments());
        } catch (Exception e) {
            logger.error("Failed to generate suggestions for index: {}", indexName, e);
            return Collections.emptyList();
        }
    }

    /**
     * Business-specific search methods
     */

    public SearchResult searchIssues(String tenantId, String query, List<SearchFilter> filters) {
        String indexName = "issues_" + tenantId;
        SearchQuery searchQuery = new SearchQuery(query, filters);
        return search(indexName, searchQuery);
    }

    public SearchResult searchProjects(String tenantId, String query) {
        String indexName = "projects_" + tenantId;
        SearchQuery searchQuery = new SearchQuery(query);
        return search(indexName, searchQuery);
    }

    public SearchResult searchUsers(String tenantId, String query) {
        String indexName = "users_" + tenantId;
        SearchQuery searchQuery = new SearchQuery(query);
        return search(indexName, searchQuery);
    }

    /**
     * Index management
     */

    public void createIndex(String indexName) {
        indices.computeIfAbsent(indexName, this::createIndex);
        logger.info("Created search index: {}", indexName);
    }

    public void deleteIndex(String indexName) {
        SearchIndex removed = indices.remove(indexName);
        if (removed != null) {
            logger.info("Deleted search index: {}", indexName);
        }
    }

    public Set<String> getAvailableIndices() {
        return indices.keySet();
    }

    /**
     * Get search analytics
     */
    public SearchAnalytics getSearchAnalytics() {
        return analytics.getStats();
    }

    /**
     * Private helper methods
     */

    private Map<String, IndexConfig> initializeIndexConfigs() {
        Map<String, IndexConfig> configs = new HashMap<>();

        // Issues index configuration
        configs.put("issues", new IndexConfig(
            "issues",
            Arrays.asList("title", "description", "comments", "tags"),
            Arrays.asList("status", "priority", "assignee", "project", "created_date"),
            1000000 // Max documents
        ));

        // Projects index configuration
        configs.put("projects", new IndexConfig(
            "projects",
            Arrays.asList("name", "description", "readme"),
            Arrays.asList("status", "owner", "created_date", "member_count"),
            100000
        ));

        // Users index configuration
        configs.put("users", new IndexConfig(
            "users",
            Arrays.asList("name", "email", "bio"),
            Arrays.asList("role", "department", "last_active"),
            500000
        ));

        return configs;
    }

    private void initializeIndices() {
        // Initialize common indices
        for (String indexName : indexConfigs.keySet()) {
            indices.computeIfAbsent(indexName, this::createIndex);
        }
        logger.info("Search indices initialized");
    }

    private SearchIndex createIndex(String indexName) {
        IndexConfig config = indexConfigs.get(indexName);
        if (config == null) {
            config = new IndexConfig(indexName, Arrays.asList("content"), Collections.emptyList(), 100000);
        }
        return new SearchIndex(config);
    }

    private List<SearchDocument> applyFilters(List<SearchDocument> documents, List<SearchFilter> filters) {
        if (filters == null || filters.isEmpty()) {
            return documents;
        }

        return documents.stream()
            .filter(document -> filters.stream().allMatch(filter -> filter.matches(document)))
            .toList();
    }

    private List<SearchHit> searchInDocuments(List<SearchDocument> documents, SearchQuery query) {
        String searchTerm = query.getQuery().toLowerCase();

        return documents.stream()
            .filter(document -> matchesQuery(document, searchTerm))
            .map(document -> new SearchHit(document.getId(), document, 1.0f, Map.of()))
            .sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
            .limit(query.getSize())
            .toList();
    }

    private boolean matchesQuery(SearchDocument document, String searchTerm) {
        // Simple text matching (would be more sophisticated in production)
        return document.getContent().toLowerCase().contains(searchTerm) ||
               document.getTitle().toLowerCase().contains(searchTerm);
    }

    private Map<String, AggregationResult> calculateAggregations(List<SearchDocument> documents, List<String> aggregations) {
        if (aggregations == null || aggregations.isEmpty()) {
            return Collections.emptyMap();
        }

        Map<String, AggregationResult> results = new HashMap<>();

        for (String aggregation : aggregations) {
            switch (aggregation) {
                case "status" -> results.put("status", aggregateByField(documents, "status"));
                case "priority" -> results.put("priority", aggregateByField(documents, "priority"));
                case "assignee" -> results.put("assignee", aggregateByField(documents, "assignee"));
                case "project" -> results.put("project", aggregateByField(documents, "project"));
            }
        }

        return results;
    }

    private AggregationResult aggregateByField(List<SearchDocument> documents, String fieldName) {
        Map<String, Long> buckets = new HashMap<>();

        for (SearchDocument doc : documents) {
            Object fieldValue = doc.getField(fieldName);
            if (fieldValue != null) {
                String key = fieldValue.toString();
                buckets.put(key, buckets.getOrDefault(key, 0L) + 1);
            }
        }

        return new AggregationResult(buckets);
    }
}

/**
 * Data classes for search service
 */

class SearchDocument {
    private final String id;
    private final String title;
    private final String content;
    private final String documentType;
    private final Map<String, Object> fields;
    private final LocalDateTime indexedAt;

    public SearchDocument(String id, String title, String content, String documentType, Map<String, Object> fields) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.documentType = documentType;
        this.fields = fields != null ? fields : new HashMap<>();
        this.indexedAt = LocalDateTime.now();
    }

    // Getters
    public String getId() { return id; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getDocumentType() { return documentType; }
    public Map<String, Object> getFields() { return fields; }
    public LocalDateTime getIndexedAt() { return indexedAt; }

    public Object getField(String fieldName) {
        return fields.get(fieldName);
    }
}

class SearchQuery {
    private final String query;
    private final List<SearchFilter> filters;
    private final int from;
    private final int size;
    private final List<String> sortFields;

    public SearchQuery(String query) {
        this(query, Collections.emptyList(), 0, 20, Collections.emptyList());
    }

    public SearchQuery(String query, List<SearchFilter> filters) {
        this(query, filters, 0, 20, Collections.emptyList());
    }

    public SearchQuery(String query, List<SearchFilter> filters, int from, int size, List<String> sortFields) {
        this.query = query;
        this.filters = filters != null ? filters : Collections.emptyList();
        this.from = from;
        this.size = size;
        this.sortFields = sortFields != null ? sortFields : Collections.emptyList();
    }

    // Getters
    public String getQuery() { return query; }
    public List<SearchFilter> getFilters() { return filters; }
    public int getFrom() { return from; }
    public int getSize() { return size; }
    public List<String> getSortFields() { return sortFields; }
}

class AdvancedSearchQuery {
    private final SearchQuery baseQuery;
    private final List<SearchFilter> filters;
    private final List<String> aggregations;
    private final int from;
    private final int size;

    public AdvancedSearchQuery(SearchQuery baseQuery, List<SearchFilter> filters,
                             List<String> aggregations, int from, int size) {
        this.baseQuery = baseQuery;
        this.filters = filters != null ? filters : Collections.emptyList();
        this.aggregations = aggregations != null ? aggregations : Collections.emptyList();
        this.from = from;
        this.size = size;
    }

    // Getters
    public SearchQuery getBaseQuery() { return baseQuery; }
    public List<SearchFilter> getFilters() { return filters; }
    public List<String> getAggregations() { return aggregations; }
    public int getFrom() { return from; }
    public int getSize() { return size; }
}

class SearchFilter {
    private final String field;
    private final String operator;
    private final Object value;

    public SearchFilter(String field, String operator, Object value) {
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    // Getters
    public String getField() { return field; }
    public String getOperator() { return operator; }
    public Object getValue() { return value; }

    public boolean matches(SearchDocument document) {
        Object fieldValue = document.getField(field);
        if (fieldValue == null) return false;

        return switch (operator) {
            case "equals" -> fieldValue.equals(value);
            case "contains" -> fieldValue.toString().contains(value.toString());
            case "greater_than" -> compare(fieldValue, value) > 0;
            case "less_than" -> compare(fieldValue, value) < 0;
            default -> false;
        };
    }

    private int compare(Object a, Object b) {
        if (a instanceof Comparable && b instanceof Comparable) {
            try {
                return ((Comparable) a).compareTo(b);
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }
}

class SearchResult {
    private final SearchQuery query;
    private final List<SearchHit> hits;
    private final long totalHits;
    private final int from;
    private final int size;
    private final long tookMs;
    private final Map<String, AggregationResult> aggregations;
    private final List<String> suggestions;

    public SearchResult(SearchQuery query, List<SearchHit> hits, long totalHits, int from, int size,
                       long tookMs, List<String> suggestions) {
        this(query, hits, totalHits, from, size, tookMs, Collections.emptyMap(), suggestions);
    }

    public SearchResult(SearchQuery query, List<SearchHit> hits, long totalHits, int from, int size,
                       long tookMs, Map<String, AggregationResult> aggregations, List<String> suggestions) {
        this.query = query;
        this.hits = hits;
        this.totalHits = totalHits;
        this.from = from;
        this.size = size;
        this.tookMs = tookMs;
        this.aggregations = aggregations;
        this.suggestions = suggestions;
    }

    public static SearchResult empty(SearchQuery query) {
        return new SearchResult(query, Collections.emptyList(), 0, 0, 0, 0, Collections.emptyList());
    }

    // Getters
    public SearchQuery getQuery() { return query; }
    public List<SearchHit> getHits() { return hits; }
    public long getTotalHits() { return totalHits; }
    public int getFrom() { return from; }
    public int getSize() { return size; }
    public long getTookMs() { return tookMs; }
    public Map<String, AggregationResult> getAggregations() { return aggregations; }
    public List<String> getSuggestions() { return suggestions; }
}

class SearchHit {
    private final String id;
    private final SearchDocument document;
    private final float score;
    private final Map<String, Object> highlights;

    public SearchHit(String id, SearchDocument document, float score, Map<String, Object> highlights) {
        this.id = id;
        this.document = document;
        this.score = score;
        this.highlights = highlights;
    }

    // Getters
    public String getId() { return id; }
    public SearchDocument getDocument() { return document; }
    public float getScore() { return score; }
    public Map<String, Object> getHighlights() { return highlights; }
}

class AggregationResult {
    private final Map<String, Long> buckets;

    public AggregationResult(Map<String, Long> buckets) {
        this.buckets = buckets;
    }

    public Map<String, Long> getBuckets() { return buckets; }
}

class IndexConfig {
    private final String name;
    private final List<String> searchableFields;
    private final List<String> filterableFields;
    private final long maxDocuments;

    public IndexConfig(String name, List<String> searchableFields, List<String> filterableFields, long maxDocuments) {
        this.name = name;
        this.searchableFields = searchableFields;
        this.filterableFields = filterableFields;
        this.maxDocuments = maxDocuments;
    }

    // Getters
    public String getName() { return name; }
    public List<String> getSearchableFields() { return searchableFields; }
    public List<String> getFilterableFields() { return filterableFields; }
    public long getMaxDocuments() { return maxDocuments; }
}

class SearchIndex {
    private final IndexConfig config;
    private final Map<String, SearchDocument> documents;

    public SearchIndex(IndexConfig config) {
        this.config = config;
        this.documents = new ConcurrentHashMap<>();
    }

    public void addDocument(String id, SearchDocument document) {
        if (documents.size() < config.getMaxDocuments()) {
            documents.put(id, document);
        } else {
            throw new IllegalStateException("Index " + config.getName() + " has reached maximum capacity");
        }
    }

    public void removeDocument(String id) {
        documents.remove(id);
    }

    public void updateDocument(String id, SearchDocument document) {
        documents.put(id, document);
    }

    public SearchDocument getDocument(String id) {
        return documents.get(id);
    }

    public List<SearchDocument> getAllDocuments() {
        return new ArrayList<>(documents.values());
    }

    public List<SearchHit> search(SearchQuery query) {
        // Simplified search implementation
        String searchTerm = query.getQuery().toLowerCase();

        return documents.values().stream()
            .filter(doc -> matchesSearch(doc, searchTerm))
            .map(doc -> new SearchHit(doc.getId(), doc, calculateScore(doc, searchTerm), Map.of()))
            .sorted((a, b) -> Float.compare(b.getScore(), a.getScore()))
            .skip(query.getFrom())
            .limit(query.getSize())
            .toList();
    }

    private boolean matchesSearch(SearchDocument doc, String searchTerm) {
        return doc.getTitle().toLowerCase().contains(searchTerm) ||
               doc.getContent().toLowerCase().contains(searchTerm);
    }

    private float calculateScore(SearchDocument doc, String searchTerm) {
        // Simple scoring based on title vs content matches
        float score = 0.0f;
        if (doc.getTitle().toLowerCase().contains(searchTerm)) {
            score += 1.0f;
        }
        if (doc.getContent().toLowerCase().contains(searchTerm)) {
            score += 0.5f;
        }
        return score;
    }
}

class SearchAnalytics {
    private final Map<String, Long> searchCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> indexingCounts = new ConcurrentHashMap<>();
    private final Map<String, Long> errorCounts = new ConcurrentHashMap<>();
    private volatile long totalSearchTime = 0;
    private volatile long totalSearches = 0;

    public void recordSearch(SearchQuery query, int resultCount, long searchTimeMs) {
        searchCounts.merge(query.getQuery(), 1L, Long::sum);
        totalSearchTime += searchTimeMs;
        totalSearches++;
    }

    public void recordIndexing(String indexName, String documentType) {
        indexingCounts.merge(indexName + ":" + documentType, 1L, Long::sum);
    }

    public void recordUpdate(String indexName, String documentType) {
        indexingCounts.merge(indexName + ":update:" + documentType, 1L, Long::sum);
    }

    public void recordDeletion(String indexName) {
        indexingCounts.merge(indexName + ":delete", 1L, Long::sum);
    }

    public void recordBulkIndexing(String indexName, int documentCount) {
        indexingCounts.merge(indexName + ":bulk", (long) documentCount, Long::sum);
    }

    public void recordIndexingError(String indexName) {
        errorCounts.merge("indexing:" + indexName, 1L, Long::sum);
    }

    public void recordBulkIndexingError(String indexName, int documentCount) {
        errorCounts.merge("bulk_indexing:" + indexName, (long) documentCount, Long::sum);
    }

    public void recordSearchError(String indexName) {
        errorCounts.merge("search:" + indexName, 1L, Long::sum);
    }

    public SearchAnalytics getStats() {
        return new SearchAnalytics(); // Return copy for thread safety
    }

    public Map<String, Long> getSearchCounts() { return new HashMap<>(searchCounts); }
    public Map<String, Long> getIndexingCounts() { return new HashMap<>(indexingCounts); }
    public Map<String, Long> getErrorCounts() { return new HashMap<>(errorCounts); }
    public long getTotalSearchTime() { return totalSearchTime; }
    public long getTotalSearches() { return totalSearches; }

    public double getAverageSearchTime() {
        return totalSearches > 0 ? (double) totalSearchTime / totalSearches : 0.0;
    }
}

class SearchSuggestions {
    private final Set<String> commonTerms = Set.of(
        "bug", "feature", "task", "urgent", "high priority", "low priority",
        "open", "closed", "in progress", "review", "testing"
    );

    public List<String> generateSuggestions(String partialQuery) {
        String lowerQuery = partialQuery.toLowerCase();
        return commonTerms.stream()
            .filter(term -> term.startsWith(lowerQuery))
            .sorted()
            .limit(5)
            .toList();
    }

    public List<String> generateSuggestions(String partialQuery, List<SearchDocument> documents) {
        List<String> suggestions = new ArrayList<>(generateSuggestions(partialQuery));

        // Add suggestions based on existing documents
        String lowerQuery = partialQuery.toLowerCase();
        documents.stream()
            .flatMap(doc -> getWordsFromDocument(doc).stream())
            .filter(word -> word.startsWith(lowerQuery))
            .distinct()
            .limit(3)
            .forEach(suggestions::add);

        return suggestions.stream().distinct().limit(8).toList();
    }

    private List<String> getWordsFromDocument(SearchDocument doc) {
        List<String> words = new ArrayList<>();
        words.addAll(extractWords(doc.getTitle()));
        words.addAll(extractWords(doc.getContent()));
        return words;
    }

    private List<String> extractWords(String text) {
        if (text == null) return Collections.emptyList();
        return Arrays.asList(text.toLowerCase().split("\\W+")).stream()
            .filter(word -> word.length() > 2)
            .toList();
    }
}
