package com.github.szczurmys.test.kafka.serdes;

import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class JsonSerdes {


    public static <T> Serde<T> json(Class<T> tClass){
        return Serdes.serdeFrom(new JsonPOJOSerializer<>(), new JsonPOJODeserializer<>(tClass));
    }

}
