package com.github.szczurmys.test.kafka.timestamp;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

public abstract class GenericTimestampExtractor<K, V> implements TimestampExtractor {

    private final Class<K> keyClazz;
    private final Class<V> valueClazz;

    protected GenericTimestampExtractor(Class<K> keyClazz, Class<V> valueClazz) {
        this.keyClazz = keyClazz;
        this.valueClazz = valueClazz;
    }

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long previousTimestamp) {

        K key = keyClazz.cast(record.key());
        V value = valueClazz.cast(record.value());

        return extract(key, value, previousTimestamp, record);
    }

    protected abstract long extract(K key, V value, long previousTimestamp, ConsumerRecord<Object, Object> originalRecord);
}
