package com.netflix.reporting.kafka;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.header.internals.RecordHeaders;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Component
public class RequestReplyGateway {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final Map<String, CompletableFuture<String>> pending = new ConcurrentHashMap<>();

    public RequestReplyGateway(KafkaTemplate<String, String> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public String request(String tenantId, String topic, String replyTopic, String key, String payload, Duration timeout) throws Exception {
        var headers = new RecordHeaders();
        KafkaHeadersUtil.setTenantId(headers, tenantId);
        String correlationId = KafkaHeadersUtil.ensureCorrelationId(headers);
        headers.add("replyTopic", replyTopic.getBytes());

        CompletableFuture<String> future = new CompletableFuture<>();
        pending.put(correlationId, future);
        try {
            ProducerRecord<String, String> record = new ProducerRecord<>(topic, null, key, payload, headers);
            kafkaTemplate.send(record);
            return future.get(timeout.toMillis(), TimeUnit.MILLISECONDS);
        } finally {
            pending.remove(correlationId);
        }
    }

    @KafkaListener(topics = "${reporting.kafka.reply-topic:reporting.replies}", groupId = "${spring.application.name:reporting-service}")
    public void onReply(ConsumerRecord<String, String> record) {
        String correlationId = KafkaHeadersUtil.get(record.headers(), KafkaHeadersUtil.CORRELATION_ID);
        if (correlationId == null) return;
        CompletableFuture<String> future = pending.get(correlationId);
        if (future != null) {
            future.complete(record.value());
        }
    }
}


