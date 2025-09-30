package com.netflix.productivity.outbox.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.netflix.productivity.outbox.entity.OutboxEvent;
import com.netflix.productivity.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxService {

    private final OutboxEventRepository repository;
    private final ObjectMapper objectMapper;

    @Value("${app.kafka.outbox.topic-audit:productivity.audit}")
    private String auditTopic;

    @Value("${app.kafka.outbox.topic-webhook:productivity.webhook}")
    private String webhookTopic;

    @Transactional
    public void enqueueAudit(String tenantId, String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        enqueue(tenantId, aggregateType, aggregateId, eventType, payload, auditTopic, aggregateId);
    }

    @Transactional
    public void enqueueWebhook(String tenantId, String aggregateType, String aggregateId, String eventType, Map<String, Object> payload) {
        enqueue(tenantId, aggregateType, aggregateId, eventType, payload, webhookTopic, aggregateId);
    }

    @Transactional
    public void enqueue(String tenantId, String aggregateType, String aggregateId, String eventType,
                        Map<String, Object> payload, String topic, String partitionKey) {
        try {
            String json = objectMapper.writeValueAsString(payload);
            OutboxEvent event = OutboxEvent.builder()
                    .tenantId(tenantId)
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(json)
                    .status("PENDING")
                    .topic(topic)
                    .partitionKey(partitionKey)
                    .createdAt(OffsetDateTime.now())
                    .availableAt(OffsetDateTime.now())
                    .build();
            repository.save(event);
        } catch (Exception e) {
            log.error("Failed to enqueue outbox event", e);
            throw new RuntimeException("Outbox enqueue failed", e);
        }
    }
}

