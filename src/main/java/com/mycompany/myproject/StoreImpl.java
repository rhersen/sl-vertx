package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import static java.util.Arrays.asList;

public class StoreImpl {
    private final HashMap<String, String> map = new HashMap<>();

    public void put(JsonObject body) {
        body.toMap().forEach((siteId, stopAreaName) ->
                        map.put(siteId, stopAreaName.toString())
        );
    }

    public JsonArray get() {
        List<Integer> list = getStations().toList();
        return new JsonArray(list.stream().map(this::wrapInObject).toArray());
    }

    private JsonArray getStations() {
        return new JsonArray(
                asList(
                        9710, 9711, 9700, 9701, 9702, 9703, 9704, 9325,
                        9502, 9503, 9504, 9505, 9506, 9507, 9508, 9509,
                        9510, 9000, 9530, 9531, 9529,
                        9528, 9527, 9526, 9525, 9524, 9523, 9522, 9521, 9520,
                        9180, 9732, 9731, 9730, 9729, 9728, 9727, 9726, 9725));
    }

    private LinkedHashMap<String, Object> wrapInObject(final Integer siteId) {
        Object found = map.get(siteId.toString());
        return new LinkedHashMap<String, Object>() {{
            put("SiteId", siteId);
            put("StopAreaName", found != null ? found : siteId.toString());
        }};
    }
}
