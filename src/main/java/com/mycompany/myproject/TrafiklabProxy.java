package com.mycompany.myproject;

import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
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
                    if (request.path().startsWith("/key")) {
                        handlePost(request);
                    } else if (request.path().startsWith("/stations")) {
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
        vertx.eventBus().send("store.get", stations, (Message<JsonArray> message) -> {
            Buffer buffer = new Buffer(message.body().encode());

            request.response()
                    .putHeader("Content-Length", Integer.toString(buffer.length()))
                    .putHeader("Content-Type", "application/json")
                    .write(buffer);
        });
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
                .get(trafiklabAddress.getUrl(request.path(), key), rsp -> rsp.bodyHandler(trafiklabData -> {
                    JsonArray array = new JsonObject(trafiklabData.toString())
                            .getObject("DPS")
                            .getObject("Trains")
                            .getArray("DpsTrain");
                    if (array == null) {
                        array = new JsonArray();
                    }
                    intercept(array);
                    String encode = array.encode();
                    Buffer buffer = new Buffer(encode);

                    request.response()
                            .putHeader("Content-Length", Integer.toString(buffer.length()))
                            .putHeader("Content-Type", "application/json")
                            .write(buffer);
                }))
                .putHeader("Accept", "application/json")
                .end();
    }

    public void intercept(JsonArray array) {
        Optional<Object> first = stream(array.spliterator(), false).findFirst();
        if (first.isPresent()) {
            JsonObject found = (JsonObject) first.get();
            LinkedHashMap<String, Object> entry = new LinkedHashMap<String, Object>() {{
                put(found.getString("SiteId"), found.getString("StopAreaName"));
            }};
            vertx.eventBus().send("store.put", new JsonObject(entry));
        }
    }

    private void handlePost(final HttpServerRequest request) {
        request
                .expectMultiPart(true)
                .bodyHandler(buffer -> vertx.eventBus().send(
                        "store",
                        request.formAttributes().get("key"),
                        (Message<String> m) -> request.response().setStatusCode(200).end()
                ));
    }
}


