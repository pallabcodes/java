package com.netflix.reporting.streams;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.Grouped;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.KTable;
import org.apache.kafka.streams.kstream.Produced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IssueAggregationTopology {

    @Bean
    public Topology issueCountsTopology() {
        StreamsBuilder builder = new StreamsBuilder();
        KStream<String, String> issues = builder.stream("${reporting.kafka.issues-topic:core.issues}", Consumed.with(Serdes.String(), Serdes.String()));

        KTable<String, Long> counts = issues
                .groupByKey(Grouped.with(Serdes.String(), Serdes.String()))
                .count();

        counts.toStream().to("${reporting.kafka.issue-counts-topic:reporting.issue-counts}", Produced.with(Serdes.String(), Serdes.Long()));

        return builder.build();
    }
}


