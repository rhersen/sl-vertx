package name.hersen.sl;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.StreamSupport.stream;

public class TrafiklabFilter {
    public static JsonObject invoke(JsonObject json, String area) {
        JsonObject responseData = json.getObject("ResponseData");

        if (responseData == null) {
            return new JsonObject();
        }

        if (Server.l != null) {
            Server.l.info(getLatestUpdate(json) + " - " + getExecutionTime(json));
        }

        Predicate<String> isJsonArray = name -> responseData.getField(name) instanceof JsonArray;

        Function<String, List<JsonObject>> jsonArrayToList =
                name -> {
                    Stream<Object> stream = stream(responseData.<JsonArray>getField(name).spliterator(), false);
                    Stream<JsonObject> objectStream = stream.filter(getFilterFor(area));
                    return objectStream.collect(toList());
                };

        Predicate<Map.Entry<String, List<JsonObject>>> nonEmpty = entry -> !entry.getValue().isEmpty();

        Stream<Map.Entry<String, List<JsonObject>>> entryStream = responseData.getFieldNames()
                .stream()
                .filter(isJsonArray)
                .collect(toMap(String::toLowerCase, jsonArrayToList))
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

    private static String getExecutionTime(JsonObject json) {
        return json.getNumber("ExecutionTime") + " ms";
    }

    private static String getLatestUpdate(JsonObject json) {
        JsonObject responseData = json.getObject("ResponseData");
        if (responseData != null) {
            return responseData.getString("LatestUpdate");
        }
        return "ResponseData missing";
    }

    private static JsonObject toJsonObject(Stream<Map.Entry<String, List<JsonObject>>> entryStream) {
        return entryStream
                .collect(JsonObject::new,
                        (accumulator, entry) -> {
                            JsonArray r = new JsonArray();
                            Stream<JsonObject> stream = entry.getValue().stream();
                            stream.forEach(r::add);
                            accumulator.putArray(entry.getKey(), r);
                        },
                        (accumulator, that) ->
                                that.getFieldNames().stream().forEach(
                                        name ->
                                                accumulator.putArray(name, that.getArray(name))));
    }

    private static Predicate getFilterFor(String area) {
        if (area == null) {
            return obj -> true;
        } else {
            return (Object obj) -> {
                Integer stopAreaNumber = ((JsonObject) obj).<Integer>getField("StopAreaNumber");
                return stopAreaNumber == null || stopAreaNumber.equals(parseInt(area));
            };
        }
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
