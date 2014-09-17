package com.mycompany.myproject;

import org.vertx.java.core.Handler;
import org.vertx.java.core.buffer.Buffer;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpClientResponse;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.platform.Verticle;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.Arrays.asList;
import static java.util.Arrays.copyOfRange;
import static java.util.stream.Collectors.*;
import static java.util.stream.StreamSupport.stream;

public class TrafiklabProxy extends Verticle {

    private final TrafiklabAddress trafiklabAddress = new TrafiklabAddress();

    public void start() {
        container.deployVerticle(Store.class.getName());
        container.deployVerticle(Nearest.class.getName());

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
                JsonObject filtered;
                if (request.params().contains("area")) {
                    filtered = filterTrafiklabData(jsonObject, request.params().get("area"));
                } else {
                    filtered = filterTrafiklabData(jsonObject, null);
                }

                intercept(filtered);

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

    public JsonObject filterTrafiklabData(JsonObject json, String area) {
        JsonObject responseData = json.getObject("ResponseData");

        Predicate<String> isJsonArray = name -> responseData.getField(name) instanceof JsonArray;

        Function<String, List> jsonArrayToList = (String name) ->
                stream(responseData.<JsonArray>getField(name).spliterator(), false)
                        .collect(toList());

        Function<Map.Entry<String, List>, List<JsonObject>> filterOnArea = e -> {
            List<JsonObject> value = e.getValue();
            return value.stream()
                    .filter(getFilterFor(area))
                    .collect(Collectors.<JsonObject>toList());
        };

        Predicate<Map.Entry<String, List<JsonObject>>> nonEmpty = entry -> !entry.getValue().isEmpty();

        Stream<Map.Entry<String, List<JsonObject>>> entryStream = responseData.getFieldNames()
                .stream()
                .filter(isJsonArray)
                .collect(toMap(String::toLowerCase, jsonArrayToList))
                .entrySet()
                .stream()
                .collect(toMap(Map.Entry::getKey, filterOnArea))
                .entrySet()
                .stream()
                .filter(nonEmpty);

        JsonObject result = toJsonObject(entryStream);

        JsonObject found = findFirstTrain(result);

        if (found != null) {
            writeHeaderFields(result, found);
        }

        return result;
    }

    private JsonObject toJsonObject(Stream<Map.Entry<String, List<JsonObject>>> entryStream) {
        return entryStream
                .collect(JsonObject::new,
                        (accumulator, entry) ->
                                accumulator.putArray(entry.getKey(), listToJson(entry.getValue())),
                        (accumulator, that) ->
                                that.getFieldNames().stream().forEach(
                                        name ->
                                                accumulator.putArray(name, that.getArray(name))));
    }

    private Predicate<JsonObject> getFilterFor(String area) {
        if (area == null) {
            return obj -> true;
        } else {
            return (JsonObject obj) -> obj.<Integer>getField("StopAreaNumber").equals(parseInt(area));
        }
    }

    private JsonArray listToJson(List<JsonObject> value) {
        JsonArray r = new JsonArray();
        value.stream().forEach(r::add);
        return r;
    }

    private JsonObject findFirstTrain(JsonObject result) {
        JsonArray trains = result.getArray("trains");
        if (trains != null) {
            Optional<Object> first = stream(trains.spliterator(), false).findFirst();
            if (first.isPresent()) {
                return (JsonObject) first.get();
            }
        }
        return result;
    }

    private void writeHeaderFields(JsonObject result, JsonObject train) {
        result.putString("StopAreaName", train.getString("StopAreaName"));
        result.putNumber("SiteId", train.getInteger("SiteId"));
    }

    public void intercept(JsonObject json) {
        JsonArray trains = json.getArray("trains");

        if (trains != null) {
            Optional<Object> first = stream(trains.spliterator(), false).findFirst();

            if (first.isPresent()) {
                JsonObject found = (JsonObject) first.get();

                vertx.eventBus().send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
                    put("" + found.getInteger("SiteId"), found.getString("StopAreaName"));
                }}));
            }
        }
    }
}
