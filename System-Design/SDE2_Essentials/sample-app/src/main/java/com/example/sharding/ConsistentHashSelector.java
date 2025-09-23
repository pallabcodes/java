package com.example.sharding;

import javax.sql.DataSource;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

public final class ConsistentHashSelector implements ShardSelector {
    private final SortedMap<Long, String> ring = new TreeMap<>();
    private final int virtualNodesPerShard;
    private final MessageDigest digest;

    public ConsistentHashSelector(Map<String, DataSource> shardIdToDs, int virtualNodesPerShard) {
        this.virtualNodesPerShard = virtualNodesPerShard;
        this.digest = initDigest();
        for (String shardId : shardIdToDs.keySet()) {
            addShard(shardId);
        }
    }

    private MessageDigest initDigest() {
        try {
            return MessageDigest.getInstance("SHA-256");
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Digest not available", e);
        }
    }

    public void addShard(String shardId) {
        for (int i = 0; i < virtualNodesPerShard; i++) {
            long hash = hashLong(shardId + "#" + i);
            ring.put(hash, shardId);
        }
    }

    public void removeShard(String shardId) {
        ring.entrySet().removeIf(e -> e.getValue().equals(shardId));
    }

    @Override
    public String selectShardId(String shardKey) {
        if (ring.isEmpty()) {
            throw new IllegalStateException("No shards configured");
        }
        long keyHash = hashLong(shardKey);
        SortedMap<Long, String> tail = ring.tailMap(keyHash);
        Long node = tail.isEmpty() ? ring.firstKey() : tail.firstKey();
        return ring.get(node);
    }

    private long hashLong(String value) {
        byte[] bytes = digest.digest(value.getBytes(StandardCharsets.UTF_8));
        ByteBuffer buf = ByteBuffer.wrap(bytes);
        return buf.getLong();
    }
}


