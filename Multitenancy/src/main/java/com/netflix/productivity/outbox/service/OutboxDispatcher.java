package com.netflix.productivity.outbox.service;

import com.netflix.productivity.outbox.entity.OutboxEvent;
import com.netflix.productivity.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
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
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.kafka.outbox.batch-size:200}")
    private int batchSize;

    @Scheduled(fixedDelayString = "${app.kafka.outbox.dispatch-interval-ms:2000}")
    @Transactional
    public void dispatch() {
        List<OutboxEvent> pending = repository.findPendingForDispatch(OffsetDateTime.now());
        int count = 0;
        for (OutboxEvent e : pending) {
            if (count >= batchSize) break;
            try {
                ProducerRecord<String, String> record = new ProducerRecord<>(e.getTopic(), e.getPartitionKey(), e.getPayload());
                kafkaTemplate.send(record).get();
                e.setStatus("SENT");
                e.setSentAt(OffsetDateTime.now());
                count++;
            } catch (Exception ex) {
                log.error("Failed to publish outbox event id={} topic={} key={}", e.getId(), e.getTopic(), e.getPartitionKey(), ex);
                // leave as PENDING for retry
            }
        }
    }
}

