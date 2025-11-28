package org.example.controller;

import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.example.dto.EventRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.annotation.PreDestroy;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@RestController
@RequestMapping("/producer")
public class ProducerController {
    @Value("${kafka.bootstrap.servers:localhost:9092}")
    private String bootstrapServers;

    @Value("${kafka.topic.name:test-topic}")
    private String topicName;

    @Value("${kafka.key.serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String keySerializer;

    @Value("${kafka.value.serializer:org.apache.kafka.common.serialization.StringSerializer}")
    private String valueSerializer;

    private final Producer<String, String> kafkaProducer;

    public ProducerController() {
        Properties props = new Properties();
        props.put("bootstrap.servers", bootstrapServers);
        props.put("key.serializer", keySerializer);
        props.put("value.serializer", valueSerializer);
        // Add security and reliability configurations
        props.put("acks", "all");
        props.put("retries", 3);
        props.put("max.in.flight.requests.per.connection", 1);
        props.put("enable.idempotence", true);

        this.kafkaProducer = new KafkaProducer<>(props);
    }

    @PostMapping("/event")
    @PreAuthorize("hasRole('PRODUCER') or hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> sendEventToKafka(@Valid @RequestBody EventRequest eventRequest) {
        String eventData = eventRequest.getEventData().trim();

        ProducerRecord<String, String> record = new ProducerRecord<>(topicName, "userEvent", eventData);

        try {
            kafkaProducer.send(record, (metadata, exception) -> {
                if (exception != null) {
                    System.err.println("Error sending message to Kafka: " + exception.getMessage());
                    // In production, you would implement proper error handling,
                    // retry logic, and dead letter queue
                } else {
                    System.out.println("Message sent to Kafka, topic: " + metadata.topic() + ", offset: " + metadata.offset());
                }
            });

            // Return success response
            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Event sent to Kafka");
            response.put("topic", topicName);
            response.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            System.err.println("Failed to send message to Kafka: " + e.getMessage());

            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "error");
            errorResponse.put("message", "Failed to send event to Kafka");
            errorResponse.put("error", e.getMessage());
            errorResponse.put("timestamp", java.time.LocalDateTime.now());

            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @PreDestroy
    public void cleanup() {
        if (kafkaProducer != null) {
            kafkaProducer.close();
        }
    }
}