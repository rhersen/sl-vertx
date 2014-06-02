package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.LinkedHashMap;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.stream.StreamSupport.stream;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress = new TrafiklabAddress();

    public void start() {
        container.deployVerticle(Store.class.getName());

        vertx.createHttpServer()
                .requestHandler(request -> {
                    if (request.path().startsWith("/stations")) {
                        handleGetStations(request);
                    } else {
                        String key = container.config().getString("trafiklab");
                        if (key == null) {
                            request.response().setStatusCode(401).end();
                        } else {
                            handleGetDeparture(request, key);
                        }
                    }
                })
                .listen(3000);
    }

    private void handleGetStations(HttpServerRequest request) {
        JsonArray stations = getStations();
        vertx.eventBus().send("store.get", stations, getReplyHandler(request));
    }

    private Handler<Message<JsonArray>> getReplyHandler(HttpServerRequest request) {
        return (Message<JsonArray> message) -> {
            Buffer buffer = new Buffer(message.body().encode());

            request.response()
                    .putHeader("Content-Length", Integer.toString(buffer.length()))
                    .putHeader("Content-Type", "application/json")
                    .write(buffer);
        };
    }

    private JsonArray getStations() {
        return new JsonArray(
                asList(9510, 9530, 9531, 9529, 9528, 9527, 9526, 9525, 9524, 9523, 9522, 9521, 9520));
    }

    private void handleGetDeparture(HttpServerRequest request, String key) {
        vertx.createHttpClient()
                .setHost("api.trafiklab.se")
                .setSSL(true)
                .setPort(443)
                .get(trafiklabAddress.getUrl(request.path(), key), getResponseHandler(request))
                .putHeader("Accept", "application/json")
                .end();
    }

    private Handler<HttpClientResponse> getResponseHandler(HttpServerRequest request) {
        return rsp -> rsp.bodyHandler(getBodyHandler(request));
    }

    private Handler<Buffer> getBodyHandler(HttpServerRequest request) {
        return trafiklabData -> {
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
        };
    }

    public void intercept(JsonArray array) {
        Optional<Object> first = stream(array.spliterator(), false).findFirst();

        if (first.isPresent()) {
            JsonObject found = (JsonObject) first.get();

            vertx.eventBus().send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
                put(found.getString("SiteId"), found.getString("StopAreaName"));
            }}));
        }
    }

}

