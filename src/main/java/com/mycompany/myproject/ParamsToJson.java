package com.mycompany.myproject;

import org.vertx.java.core.MultiMap;
import org.vertx.java.core.json.JsonObject;

import static java.util.stream.StreamSupport.stream;

public class ParamsToJson {
    public org.vertx.java.core.json.JsonObject invoke(MultiMap params) {
        return stream(params.spliterator(), false)
                .collect(JsonObject::new,
                        (accumulator, entry) -> accumulator.putNumber(entry.getKey(), Double.valueOf(entry.getValue())),
                        (accumulator, that) ->
                                that.getFieldNames().stream().forEach(
                                        name ->
                                                accumulator.putNumber(name, Double.valueOf(that.getString(name)))));
    }
}
