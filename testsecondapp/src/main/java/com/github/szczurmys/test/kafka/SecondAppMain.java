package com.github.szczurmys.test.kafka;

import com.github.szczurmys.test.kafka.model.Key;
import com.github.szczurmys.test.kafka.model.Value;
import com.github.szczurmys.test.kafka.serdes.JsonSerdes;
import com.github.szczurmys.test.kafka.timestamp.GenericTimestampExtractor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class SecondAppMain {
    private static final Logger logger = LoggerFactory.getLogger(SecondAppMain.class);

    public static void main(final String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.stream.second");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.0.75.2:31091,10.0.75.2:31092,10.0.75.2:31093");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, TimeUnit.MINUTES.toMillis(1));

        StreamsBuilder builder = new StreamsBuilder();
        KStream<Key, Value> input = builder.stream(
                "kafka.stream.second",
                Consumed.<Key, Value>with(Topology.AutoOffsetReset.EARLIEST)
                        .withKeySerde(JsonSerdes.json(Key.class))
                        .withValueSerde(JsonSerdes.json(Value.class))
                        .withTimestampExtractor(new GenericTimestampExtractor<Key, Value>(Key.class, Value.class) {
                            @Override
                            protected long extract(Key key, Value value, long previousTimestamp, ConsumerRecord<Object, Object> originalRecord) {
                                return value.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            }
                        })
        );

        input.groupByKey()

                .windowedBy(TimeWindows
                        .of(TimeUnit.MINUTES.toMillis(5))
                        .until(TimeUnit.MINUTES.toMillis(5))

                )
                .reduce((value1, value2) ->
                                new Value(value1.getValue().add(value2.getValue()), max(value1.getLocalDateTime(), value2.getLocalDateTime())),
                        Materialized.with(
                                JsonSerdes.json(Key.class),
                                JsonSerdes.json(Value.class)
                        ))
                .toStream((key, value) -> key.key())
                .map((key, value) -> new KeyValue<>(key, new Value(
                        value.getValue(),
                        value.getLocalDateTime(),
                        LocalDateTime.now()
                )))
                .peek((key, value) -> logger.info("Data from second: key={}; value={}", key, value))
                .to("kafka.stream.third", Produced.with(JsonSerdes.json(Key.class), JsonSerdes.json(Value.class)));


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

    private static LocalDateTime max(LocalDateTime v1, LocalDateTime v2) {
        return Stream.of(v1, v2).max(LocalDateTime::compareTo).orElse(null);
    }

}
