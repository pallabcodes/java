package com.netflix.productivity.outbox.service;

import com.netflix.productivity.outbox.entity.OutboxEvent;
import com.netflix.productivity.outbox.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxAdminService {

    private final OutboxEventRepository repository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Transactional(readOnly = true)
    public List<OutboxEvent> listDlq() {
        return repository.findTop100ByStatusOrderByCreatedAtAsc("DLQ");
        }

    @Transactional
    public int replayDlqBatch() {
        List<OutboxEvent> dlq = repository.findTop100ByStatusOrderByCreatedAtAsc("DLQ");
        int count = 0;
        for (OutboxEvent e : dlq) {
            kafkaTemplate.send(new ProducerRecord<>(e.getTopic(), e.getPartitionKey(), e.getPayload()));
            e.setStatus("PENDING");
            count++;
        }
        return count;
    }
}
