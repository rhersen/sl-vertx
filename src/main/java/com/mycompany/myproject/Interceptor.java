package com.mycompany.myproject;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Optional;

import static java.util.stream.StreamSupport.stream;

public class Interceptor {
    private EventBus eventBus;

    public Interceptor(EventBus eventBus) {
        this.eventBus = eventBus;
    }

    public void invoke(JsonObject json) {
        JsonArray trains = json.getArray("trains");

        if (trains != null) {
            Optional<Object> first = stream(trains.spliterator(), false).findFirst();

            if (first.isPresent()) {
                JsonObject found = (JsonObject) first.get();

                eventBus.send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
                    put("" + found.getInteger("SiteId"), found.getString("StopAreaName"));
                }}));
            }
        }
    }
}
