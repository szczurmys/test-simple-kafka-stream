package com.github.szczurmys.test.kafka;

import com.github.szczurmys.test.kafka.serdes.JsonSerdes;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ThirdAppMain {
    private static final Logger logger = LoggerFactory.getLogger(ThirdAppMain.class);

    public static void main(final String[] args) {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.stream.third");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:31091,localhost:31092,localhost:31093");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        props.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, "exactly_once");

        props.put(StreamsConfig.STATE_DIR_CONFIG, "./target/test-kafka-state");

        props.put(StreamsConfig.REPLICATION_FACTOR_CONFIG, 3);

        String thirdStateStoreName = "test.kafka.stream.third.state";

        StreamsBuilder builder = new StreamsBuilder();
        KStream<Integer, String> input = builder.stream(
                "kafka.stream.fourth",
                Consumed.<Integer, String>with(Topology.AutoOffsetReset.EARLIEST)
                        .withKeySerde(JsonSerdes.json(Integer.class))
                        .withValueSerde(Serdes.String())
        );

        input
                .filter((key, value) -> hasCorrectType(value))
                .mapValues(value -> {
                    if (value.equalsIgnoreCase("check")) {
                        return 0L;
                    }
                    if (value.equalsIgnoreCase("new")) {
                        return 1L;
                    }
                    if (value.equalsIgnoreCase("markRead")) {
                        return -1L;
                    }
                    return 0L;

                })
                .groupByKey()
                .reduce(Long::sum,
                        Materialized.<Integer, Long, KeyValueStore<Bytes, byte[]>>as(thirdStateStoreName)
                                .withKeySerde(JsonSerdes.json(Integer.class))
                                .withValueSerde(JsonSerdes.json(Long.class))
                                .withCachingEnabled()
                )
                .toStream((key, value) -> key)
                .peek((key, value) -> logger.info("Data from third: key={}; value={}", key, value))
                .to("kafka.stream.fifth", Produced.with(JsonSerdes.json(Integer.class), JsonSerdes.json(Long.class)));


        final KafkaStreams streams = new KafkaStreams(builder.build(), props);
        final CountDownLatch latch = new CountDownLatch(1);
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Close kafka-stream.");
            streams.close();
            latch.countDown();
        }));
        try {
            logger.info("Start kafka-stream.");
            streams.start();
            latch.await();
        } catch (Throwable e) {
            logger.error("Error kafka-stream.", e);
            System.exit(1);
        }
        System.exit(0);
    }

    private static boolean hasCorrectType(String type) {
        return type != null
                && (type.equalsIgnoreCase("new")
                || type.equalsIgnoreCase("markRead")
                || type.equalsIgnoreCase("check"));
    }
}
