package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Optional;

import static java.util.stream.StreamSupport.stream;

public class BodyHandler implements Handler<Buffer> {
    private HttpServerRequest request;
    private EventBus vertx;

    public BodyHandler(HttpServerRequest request, EventBus vertx) {
        this.request = request;
        this.vertx = vertx;
    }

    public void handle(Buffer trafiklabData) {
        JsonArray array = new JsonObject(trafiklabData.toString())
                .getObject("DPS")
                .getObject("Trains")
                .getArray("DpsTrain");

        if (array == null) {
            array = new JsonArray();
        }

        intercept(array);

        Buffer buffer = new Buffer(array.encode());

        request.response()
                .putHeader("Content-Length", Integer.toString(buffer.length()))
                .putHeader("Content-Type", "application/json")
                .write(buffer);
    }

    public void intercept(JsonArray array) {
        Optional<Object> first = stream(array.spliterator(), false).findFirst();

        if (first.isPresent()) {
            JsonObject found = (JsonObject) first.get();

            vertx.send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
                put(found.getString("SiteId"), found.getString("StopAreaName"));
            }}));
        }
    }
}
