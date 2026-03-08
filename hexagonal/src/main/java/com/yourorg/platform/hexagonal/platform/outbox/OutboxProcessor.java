package com.yourorg.platform.hexagonal.platform.outbox;

import com.yourorg.platform.hexagonal.application.port.out.OutboxMessage;
import com.yourorg.platform.hexagonal.application.port.out.OutboxPort;
import com.yourorg.platform.hexagonal.platform.config.AppProperties;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "app.outbox.polling-enabled", havingValue = "true")
public class OutboxProcessor {
    private static final Logger log = LoggerFactory.getLogger(OutboxProcessor.class);

    private final OutboxPort outboxPort;
    private final AppProperties properties;

    public OutboxProcessor(OutboxPort outboxPort, AppProperties properties) {
        this.outboxPort = outboxPort;
        this.properties = properties;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:5000}")
    public void poll() {
        List<OutboxMessage> messages = outboxPort.findUnprocessed(properties.getOutbox().getBatchSize());
        if (messages.isEmpty()) {
            return;
        }
        log.info("Outbox poll found {} messages", messages.size());
        // TODO: publish to broker or let CDC handle it.
        outboxPort.markProcessed(messages);
    }
}
