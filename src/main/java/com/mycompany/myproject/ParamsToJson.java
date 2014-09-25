package com.mycompany.myproject;

import org.vertx.java.core.MultiMap;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;

public class ParamsToJson {
    public String invoke(MultiMap params) {
        return asList("latitude", "longitude", "limit").stream()
                .map(params::get)
                .filter(value -> value != null)
                .collect(joining(","));
    }
}
