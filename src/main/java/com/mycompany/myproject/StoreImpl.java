package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StoreImpl {
    private final HashMap<String, String> map = new HashMap<>();
    private List<Object> stations = Collections.emptyList();

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
        return new JsonArray(stations);
    }

    private LinkedHashMap<String, Object> wrapInObject(final Integer siteId) {
        Object found = map.get(siteId.toString());
        return new LinkedHashMap<String, Object>() {{
            put("SiteId", siteId);
            put("StopAreaName", found != null ? found : siteId.toString());
        }};
    }

    public void setStations(List<Object> stations) {
        this.stations = stations;
    }
}
