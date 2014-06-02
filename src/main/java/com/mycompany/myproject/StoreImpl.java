package com.mycompany.myproject;

import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

public class StoreImpl {
    private final HashMap<String, String> map = new HashMap<>();

    public void put(JsonObject body) {
        map.put(body.getString("SiteId"), body.getString("StopAreaName"));
    }

    public JsonArray get(JsonArray siteIds) {
        List<Integer> list = siteIds.toList();
        return new JsonArray(list.stream().map(this::wrapInObject).toArray());
    }

    private LinkedHashMap<String, Object> wrapInObject(final Integer siteId) {
        Object found = map.get(siteId.toString());
        return new LinkedHashMap<String, Object>() {{
            put("SiteId", siteId);
            put("StopAreaName", found != null ? found : siteId.toString());
        }};
    }
}
