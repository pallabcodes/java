package com.netflix.productivity.outbox.service;

import com.netflix.productivity.outbox.entity.OutboxEvent;
import com.netflix.productivity.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import com.netflix.productivity.avro.AuditEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.kafka.outbox.batch-size:200}")
    private int batchSize;

    @Value("${app.kafka.outbox.topic-audit:productivity.audit}")
    private String auditTopic;

    @Value("${app.kafka.outbox.dlq-topic:productivity.outbox.dlq}")
    private String dlqTopic;

    @Scheduled(fixedDelayString = "${app.kafka.outbox.dispatch-interval-ms:2000}")
    @Transactional
    public void dispatch() {
        List<OutboxEvent> pending = repository.findPendingForDispatch(OffsetDateTime.now());
        int count = 0;
        for (OutboxEvent e : pending) {
            if (count >= batchSize) break;
            try {
                ProducerRecord<String, Object> record;
                if (auditTopic.equals(e.getTopic())) {
                    AuditEvent avro = AuditEvent.newBuilder()
                            .setTenantId(e.getTenantId())
                            .setAggregateType(e.getAggregateType())
                            .setAggregateId(e.getAggregateId())
                            .setEventType(e.getEventType())
                            .setOccurredAt(e.getCreatedAt().toInstant().toEpochMilli())
                            .setPayload(e.getPayload())
                            .build();
                    record = new ProducerRecord<>(e.getTopic(), e.getPartitionKey(), avro);
                } else {
                    // Fallback to JSON for non-audit topics
                    record = new ProducerRecord<>(e.getTopic(), e.getPartitionKey(), e.getPayload());
                }
                kafkaTemplate.send(record).get();
                e.setStatus("SENT");
                e.setSentAt(OffsetDateTime.now());
                count++;
            } catch (Exception ex) {
                log.error("Failed to publish outbox event id={} topic={} key={}", e.getId(), e.getTopic(), e.getPartitionKey(), ex);
                // send to DLQ for inspection
                try {
                    ProducerRecord<String, Object> dlqRecord = new ProducerRecord<>(dlqTopic, e.getPartitionKey(), e.getPayload());
                    kafkaTemplate.send(dlqRecord).get();
                    e.setStatus("DLQ");
                    e.setSentAt(OffsetDateTime.now());
                } catch (Exception dlqEx) {
                    log.error("Failed to publish to DLQ for outbox id={}", e.getId(), dlqEx);
                    // leave as PENDING for retry cycle
                }
            }
        }
    }
}

