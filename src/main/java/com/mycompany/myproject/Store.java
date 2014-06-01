package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Store extends Verticle implements Handler<Message<JsonObject>> {
    private String key = "";

    public void start() {
        vertx.eventBus().registerHandler("store", this);
    }

    String get() {
        return key;
    }

    public void put(String key) {
        this.key = key;
    }

    public void handle(Message<JsonObject> message) {
        JsonObject body = message.body();
        System.out.println("received" + body);
    }
}
