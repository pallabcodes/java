package com.example.sharding;

import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class ConsistentHashSelectorTest {

    @Test
    void distributesKeysAndIsStable() {
        Map<String, DataSource> m = new HashMap<>();
        m.put("A", ds());
        m.put("B", ds());
        m.put("C", ds());
        ConsistentHashSelector sel = new ConsistentHashSelector(m, 32);

        int countA = 0, countB = 0, countC = 0;
        for (int i = 0; i < 3000; i++) {
            String shard = sel.selectShardId("k" + i);
            if ("A".equals(shard)) countA++; else if ("B".equals(shard)) countB++; else if ("C".equals(shard)) countC++;
        }

        int avg = (countA + countB + countC) / 3;
        assertTrue(Math.abs(countA - avg) < avg * 0.25);
        assertTrue(Math.abs(countB - avg) < avg * 0.25);
        assertTrue(Math.abs(countC - avg) < avg * 0.25);

        String s1 = sel.selectShardId("customer-123");
        String s2 = sel.selectShardId("customer-123");
        assertEquals(s1, s2);
    }

    private DataSource ds() { return new org.h2.jdbcx.JdbcDataSource(); }
}


