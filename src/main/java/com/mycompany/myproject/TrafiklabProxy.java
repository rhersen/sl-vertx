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
import static java.util.Arrays.copyOfRange;
import static java.util.stream.StreamSupport.stream;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress = new TrafiklabAddress();

    public void start() {
        container.deployVerticle(Store.class.getName());

        vertx.createHttpServer()
                .requestHandler(request -> {
                    if (request.path().equals("/favicon.ico")) {
                        try {
                            byte[] bytes = new byte[4096];
                            int n = getClass().getResourceAsStream("/J.png").read(bytes);
                            respondWith(new Buffer(copyOfRange(bytes, 0, n)), "image/png", request);
                        } catch (Exception e) {
                            // ignore
                        }
                    } else if (request.path().startsWith("/stations")) {
                        handleGetStations(request);
                    } else {
                        String key = container.config().getString("trafiklab");
                        if (key == null) {
                            request.response().setStatusCode(401).end();
                        } else {
                            handleGetDepartures(request, key);
                        }
                    }
                })
                .listen(3000);
    }

    private void handleGetStations(HttpServerRequest request) {
        vertx.eventBus().send("store.stations", "", getReplyHandler(request));
    }

    private Handler<Message<JsonObject>> getReplyHandler(HttpServerRequest request) {
        return (Message<JsonObject> message) ->
                respondWith(new Buffer(message.body().encode()), "application/json", request);
    }

    private void respondWith(Buffer buffer, String contentType, HttpServerRequest request) {
        request.response()
                .putHeader("Content-Length", Integer.toString(buffer.length()))
                .putHeader("Content-Type", contentType)
                .end(buffer);
    }

    private void handleGetDepartures(HttpServerRequest request, String key) {
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
            JsonObject jsonObject = new JsonObject(trafiklabData.toString());
            JsonObject filtered = filterTrafiklabData(jsonObject);

            intercept(filtered);

            Buffer buffer = new Buffer(filtered.encode());

            request.response()
                    .putHeader("Content-Length", Integer.toString(buffer.length()))
                    .putHeader("Content-Type", "application/json")
                    .write(buffer);
        };
    }

    public JsonObject filterTrafiklabData(JsonObject json) {
        JsonArray array = json
                .getObject("DPS")
                .getObject("Trains")
                .getArray("DpsTrain");

        if (array == null) {
            array = new JsonArray();
        }

        JsonObject filtered = new JsonObject();
        filtered.putArray("trains", array);

        Optional<Object> first = stream(array.spliterator(), false).findFirst();
        if (first.isPresent()) {
            JsonObject train = (JsonObject) first.get();
            asList("SiteId", "StopAreaName").stream().forEach(key -> filtered.putString(key, train.getString(key)));
        }

        return filtered;
    }

    public void intercept(JsonObject json) {
        Optional<Object> first = stream(json.getArray("trains").spliterator(), false).findFirst();

        if (first.isPresent()) {
            JsonObject found = (JsonObject) first.get();

            vertx.eventBus().send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
                put(found.getString("SiteId"), found.getString("StopAreaName"));
            }}));
        }
    }

}


