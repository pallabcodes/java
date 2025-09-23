package com.example.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Map;

@Configuration
public class DataInit {
    @Bean
    CommandLineRunner initShards(Map<String, DataSource> shardMap) {
        return args -> {
            for (Map.Entry<String, DataSource> e : shardMap.entrySet()) {
                try (Connection c = e.getValue().getConnection(); Statement s = c.createStatement()) {
                    s.executeUpdate("create table if not exists account (id varchar(64) primary key, name varchar(128), balance bigint)");
                    if ("shardA".equals(e.getKey())) {
                        s.executeUpdate("merge into account key(id) values ('a1','Alice',1000)");
                        s.executeUpdate("merge into account key(id) values ('a2','Aiden',2000)");
                    } else if ("shardB".equals(e.getKey())) {
                        s.executeUpdate("merge into account key(id) values ('b1','Bob',1500)");
                        s.executeUpdate("merge into account key(id) values ('b2','Bianca',2500)");
                    }
                }
            }
        };
    }
}


