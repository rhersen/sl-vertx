package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.joining;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress = new TrafiklabAddress();
    private Interceptor interceptor;

    public void start() {
        container.deployVerticle(Store.class.getName());
        container.deployVerticle(Nearest.class.getName());

        interceptor = new Interceptor(vertx.eventBus());

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
                    } else if (request.path().startsWith("/nearest")) {
                        handleNearest(request);
                    } else {
                        String key = container.config().getString("sl");
                        if (key == null) {
                            request.response().setStatusCode(401).end();
                        } else try {
                            handleGetDepartures(request, key);
                        } catch (Exception e) {
                            request.response()
                                    .setStatusCode(400)
                                    .setStatusMessage("Bad Request")
                                    .putHeader("Content-Length", Integer.toString(e.getMessage().length()))
                                    .write(e.getMessage())
                                    .end();
                        }
                    }
                })
                .listen(3000);
    }

    private void handleGetStations(HttpServerRequest request) {
        vertx.eventBus().send("store.stations", "", getObjectReplyHandler(request));
    }

    private void handleNearest(HttpServerRequest request) {
        String position = asList("latitude", "longitude").stream()
                .map(name -> request.params().get(name))
                .collect(joining(","));
        vertx.eventBus().send("nearest", position, getArrayReplyHandler(request));
    }

    private Handler<Message<JsonObject>> getObjectReplyHandler(HttpServerRequest request) {
        return (Message<JsonObject> message) ->
                respondWith(new Buffer(message.body().encode()), "application/json", request);
    }

    private Handler<Message<JsonArray>> getArrayReplyHandler(HttpServerRequest request) {
        return (Message<JsonArray> message) ->
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
                .setHost("api.sl.se")
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
            try {
                JsonObject jsonObject = new JsonObject(trafiklabData.toString());
                JsonObject filtered = TrafiklabFilter.invoke(jsonObject, request.params().get("area"));

                interceptor.invoke(filtered);

                Buffer buffer = new Buffer(filtered.encode());

                request.response()
                        .putHeader("Content-Length", Integer.toString(buffer.length()))
                        .putHeader("Content-Type", "application/json")
                        .write(buffer);
            } catch (Exception e) {
                e.printStackTrace();
                request.response().setStatusCode(500).setStatusMessage("Internal server error").end();
            }
        };
    }
}

