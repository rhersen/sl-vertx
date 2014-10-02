package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import static java.util.Arrays.copyOfRange;

public class Server extends Verticle {

    private final TrafiklabAddress trafiklabAddress = new TrafiklabAddress();
    private Interceptor interceptor;

    public void start() {
        container.deployVerticle(Store.class.getName());
        container.deployVerticle(Nearest.class.getName(), container.config());

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
                        String key = container.config().getString("realtimedepartures");
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
        vertx.eventBus().send("store.stations", "", sendObjectResponseTo(request));
    }

    private void handleNearest(HttpServerRequest request) {
        vertx.eventBus().send("nearest", new ParamsToJson().invoke(request.params()), sendArrayResponseTo(request));
    }

    private Handler<Message<JsonObject>> sendObjectResponseTo(HttpServerRequest request) {
        return (Message<JsonObject> message) ->
                respondWith(new Buffer(message.body().encode()), "application/json", request);
    }

    private Handler<Message<JsonArray>> sendArrayResponseTo(HttpServerRequest request) {
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
                .get(trafiklabAddress.getUrl(request.path(), key), rsp -> {
                    if (rsp.statusCode() == 200) {
                        rsp.bodyHandler(getBodyHandler(request));
                    } else {
                        System.err.println(rsp.statusCode() + " " + rsp.statusMessage());
                        request.response().setStatusCode(rsp.statusCode()).end();
                    }
                })
                .putHeader("Accept", "application/json")
                .end();
    }

    private Handler<Buffer> getBodyHandler(HttpServerRequest request) {
        return trafiklabData -> {
            try {
                JsonObject jsonObject = new JsonObject(trafiklabData.toString());
                JsonObject filtered = TrafiklabFilter.invoke(jsonObject, request.params().get("area"));
                Integer statusCode = jsonObject.getInteger("StatusCode");

                if (statusCode == null || statusCode == 0) {
                    interceptor.invoke(filtered);

                    Buffer buffer = new Buffer(filtered.encode());

                    request.response()
                            .putHeader("Content-Length", Integer.toString(buffer.length()))
                            .putHeader("Content-Type", "application/json")
                            .write(buffer);
                } else if (statusCode == 1006) {
                    System.err.println(jsonObject.getString("Message"));
                    request.response().setStatusCode(429).end();
                }
            } catch (Exception e) {
                e.printStackTrace();
                request.response().setStatusCode(500).setStatusMessage("Internal server error").end();
            }
        };
    }
}

