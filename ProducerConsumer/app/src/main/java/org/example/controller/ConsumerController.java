package org.example.controller;

import io.prometheus.client.Counter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class ConsumerController {

    @Value("${kafka.topic.name:test-topic}")
    private String topicName;

    @Value("${kafka.consumer.group.id:metrics-consumer-group}")
    private String groupId;

    private final Counter kafkaEventsCounter;

    public ConsumerController() {
        kafkaEventsCounter = Counter.build()
                .name("kafka_events_received_total")
                .help("Total number of Kafka events received")
                .labelNames("topic", "group_id")
                .register();
    }

    @KafkaListener(topics = "#{__listener.topicName}", groupId = "#{__listener.groupId}")
    public void listen(String eventData) {
        if (eventData == null || eventData.trim().isEmpty()) {
            System.err.println("Received null or empty event data");
            return;
        }

        System.out.println("Received event: " + eventData);
        kafkaEventsCounter.labels(topicName, groupId).inc();

        // In production, you would process the event here
        // This could include validation, transformation, and forwarding to other services
    }

    // Getter methods for SpEL expressions in @KafkaListener
    public String getTopicName() {
        return topicName;
    }

    public String getGroupId() {
        return groupId;
    }
}