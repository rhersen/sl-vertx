package com.mycompany.myproject;

import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

public class Store extends Verticle {

    private StoreImpl store;

    public void start() {
        store = new StoreImpl();
        vertx.eventBus().registerHandler("store.put", (Message<JsonObject> message) -> store.put(message.body()));
        vertx.eventBus().registerHandler("store.stations", (Message m) -> m.reply(store.get()));
    }
}
