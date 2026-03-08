package com.yourorg.platform.clean.usecase.port;

import java.util.List;

public interface OutboxPort {
    void save(OutboxMessage message);
    List<OutboxMessage> findUnprocessed(int limit);
    void markProcessed(List<OutboxMessage> messages);
}
