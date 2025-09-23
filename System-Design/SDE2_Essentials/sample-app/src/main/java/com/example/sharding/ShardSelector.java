package com.example.sharding;

public interface ShardSelector {
    String selectShardId(String shardKey);
}


