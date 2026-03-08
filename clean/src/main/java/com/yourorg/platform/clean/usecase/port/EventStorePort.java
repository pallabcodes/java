package com.yourorg.platform.clean.usecase.port;

import java.util.List;

public interface EventStorePort {
    void append(String streamId, String eventType, String payload);
    List<String> load(String streamId);
}
