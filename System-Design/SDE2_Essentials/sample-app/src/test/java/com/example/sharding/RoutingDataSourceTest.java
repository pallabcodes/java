package com.example.sharding;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class RoutingDataSourceTest {

    @Test
    void contextSetAndClear() {
        RoutingDataSource.setCurrentShardId("X");
        RoutingDataSource ds = new RoutingDataSource();
        assertEquals("X", ds.determineCurrentLookupKey());
        RoutingDataSource.clear();
        assertNull(ds.determineCurrentLookupKey());
    }
}


