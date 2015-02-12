package name.hersen.sl;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.lang.Integer.parseInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

public class TrafiklabFilter {

    private static final Pattern date = Pattern.compile("\\d\\d\\d\\d-\\d\\d-\\d\\dT(\\d\\d):(\\d\\d):\\d\\d");

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

        setUniqueKeysOn(result.getArray("trains"));
        writeHeaderFields(result);

        return result;
    }

    private static void setUniqueKeysOn(JsonArray trains) {
        if (trains == null) {
            return;
        }

        stream(trains.spliterator(), false).forEach(o -> {
            JsonObject train = (JsonObject) o;
            String timeTabledDateTime = "" + train.getString("TimeTabledDateTime");
            Matcher m = date.matcher(timeTabledDateTime);

            train.putString("Key", m.matches() ? m.group(1) + m.group(2) + train.getInteger("JourneyDirection") : timeTabledDateTime);
        });

        List<String> keyList = stream(trains.spliterator(), false)
                .map(o -> ((JsonObject) o).getString("Key"))
                .collect(toList());

        Set<String> keySet = stream(trains.spliterator(), false)
                .map(o -> ((JsonObject) o).getString("Key"))
                .collect(toSet());

        if (keyList.size() != keySet.size()) {
            throw new IllegalStateException("duplicate keys in " + keyList);
        }
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

    private static void writeHeaderFields(JsonObject result) {
        JsonObject train = findFirstTrain(result);

        if (train == null) {
            return;
        }

        result.putString("StopAreaName", train.getString("StopAreaName"));
        result.putNumber("SiteId", train.getInteger("SiteId"));
    }
}
