package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.StoreImpl;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class StoreImplTest {

    private StoreImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new StoreImpl();
    }

    @Test
    public void shouldReturnSiteIdAsStopAreaNameIfEmpty() throws Exception {
        JsonArray result = subject.get(new JsonArray(asList(9526, 9527)));
        assertEquals("9526", result.<JsonObject>get(0).getString("StopAreaName"));
        assertEquals("9527", result.<JsonObject>get(1).getString("StopAreaName"));
    }

    @Test
    public void shouldReturnStopAreaNameIfItHasBeenPut() throws Exception {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("SiteId", "9525");
        map.put("StopAreaName", "Tullinge");

        subject.put(new JsonObject(map));
        JsonArray result = subject.get(new JsonArray(asList(9525, 9527)));

        assertEquals("Tullinge", result.<JsonObject>get(0).getString("StopAreaName"));
    }
}