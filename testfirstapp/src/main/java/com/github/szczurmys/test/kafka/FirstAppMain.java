package com.github.szczurmys.test.kafka;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.github.szczurmys.test.kafka.model.DataListDto;
import com.github.szczurmys.test.kafka.model.Key;
import com.github.szczurmys.test.kafka.model.Value;
import com.github.szczurmys.test.kafka.serdes.JsonSerdes;
import com.github.szczurmys.test.kafka.timestamp.GenericTimestampExtractor;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.*;
import org.apache.kafka.streams.kstream.Consumed;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Produced;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.ZoneId;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;
import java.util.stream.Collectors;

public class FirstAppMain {
    private static final Logger logger = LoggerFactory.getLogger(FirstAppMain.class);
@JsonIgnore
    public static void main(final String[] args) throws Exception {
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "kafka.stream.first");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "10.0.75.2:31091,10.0.75.2:31092,10.0.75.2:31093");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();
        KStream<Object, DataListDto> input = builder.stream(
                "kafka.stream.first",
                Consumed.<Object, DataListDto>with(Topology.AutoOffsetReset.EARLIEST)
                        .withValueSerde(JsonSerdes.json(DataListDto.class))
                        .withTimestampExtractor(new GenericTimestampExtractor<Object, DataListDto>(Object.class, DataListDto.class) {
                            @Override
                            protected long extract(Object key, DataListDto value, long previousTimestamp, ConsumerRecord<Object, Object> originalRecord) {
                                return value.getLocalDateTime().atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
                            }
                        })

        );

        input.flatMap((key, value) ->
                value.getElements().stream()
                        .map(v -> new KeyValue<>(
                                new Key(value.getSite(), value.getLocalDateTime().toLocalDate(), v.getProduct()),
                                new Value(v.getValue(), value.getLocalDateTime())
                        ))
                        .collect(Collectors.toList())
        )
                .peek((key, value) -> logger.info("Data from first: key={}; value={}", key, value))
                .to("kafka.stream.second", Produced.with(JsonSerdes.json(Key.class), JsonSerdes.json(Value.class)));


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

}
