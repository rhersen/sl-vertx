package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.StoreImpl;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertEquals;

public class StoreImplTest {

    private StoreImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new StoreImpl();
    }

    @Test
    public void shouldReturnSiteIdAsStopAreaNameIfEmpty() throws Exception {
        JsonArray result = subject.get();
        assertEquals("9710", result.<JsonObject>get(0).getString("StopAreaName"));
        assertEquals("9711", result.<JsonObject>get(1).getString("StopAreaName"));
    }

    @Test
    public void shouldReturnStopAreaNameIfItHasBeenPut() throws Exception {
        subject.put(new JsonObject(new LinkedHashMap<String, Object>() {{
            put("9525", "Tullinge");
        }}));

        JsonArray result = subject.get();

        assertEquals("Tullinge", result.<JsonObject>get(24).getString("StopAreaName"));
    }
}