package com.netflix.productivity.repository.projection;

public interface SearchHitProjection {
    String getId();
    String getKey();
    String getTitle();
    Double getScore();
}

