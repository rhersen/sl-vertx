package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

public class TrafiklabFilter {
    public static JsonObject invoke(JsonObject json, String area) {
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

    private static JsonObject toJsonObject(Stream<Map.Entry<String, List<JsonObject>>> entryStream) {
        return entryStream
                .collect(JsonObject::new,
                        (accumulator, entry) ->
                                accumulator.putArray(entry.getKey(), listToJson(entry.getValue())),
                        (accumulator, that) ->
                                that.getFieldNames().stream().forEach(
                                        name ->
                                                accumulator.putArray(name, that.getArray(name))));
    }

    private static Predicate<JsonObject> getFilterFor(String area) {
        if (area == null) {
            return obj -> true;
        } else {
            return (JsonObject obj) -> obj.<Integer>getField("StopAreaNumber").equals(parseInt(area));
        }
    }

    private static JsonArray listToJson(List<JsonObject> value) {
        JsonArray r = new JsonArray();
        value.stream().forEach(r::add);
        return r;
    }

    private static JsonObject findFirstTrain(JsonObject result) {
        JsonArray trains = result.getArray("trains");
        if (trains != null) {
            Optional<Object> first = stream(trains.spliterator(), false).findFirst();
            if (first.isPresent()) {
                return (JsonObject) first.get();
            }
        }
        return result;
    }

    private static void writeHeaderFields(JsonObject result, JsonObject train) {
        result.putString("StopAreaName", train.getString("StopAreaName"));
        result.putNumber("SiteId", train.getInteger("SiteId"));
    }
}
