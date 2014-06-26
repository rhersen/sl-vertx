package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.*;

import static java.util.Arrays.asList;

public class StoreImpl {
    private final Map<String, String> map = new HashMap<>();
    private Map<String, List<Integer>> stations = new HashMap<>();

    public void put(JsonObject body) {
        body.toMap().forEach(
                (siteId, stopAreaName) ->
                        map.put(siteId, stopAreaName.toString())
        );
    }

    public JsonObject get() {
        return asList("northwest", "northeast", "central", "southwest", "southeast")
                .stream()
                .reduce(new JsonObject(),
                        (JsonObject acc, String key) ->
                                acc.putArray(
                                        key,
                                        new JsonArray(
                                                stations
                                                        .get(key)
                                                        .stream()
                                                        .map(this::wrapInObject)
                                                        .toArray())),
                        JsonObject::mergeIn);
    }

    private LinkedHashMap<String, Object> wrapInObject(final Integer siteId) {
        Object found = map.get(siteId.toString());
        return new LinkedHashMap<String, Object>() {{
            put("SiteId", siteId);
            put("StopAreaName", found != null ? found : siteId.toString());
        }};
    }

    public List<Integer> putStations(String key, List<Integer> list) {
        return stations.put(key, list);
    }
}
