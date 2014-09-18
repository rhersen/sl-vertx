package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.TrafiklabFilter;
import com.mycompany.myproject.TrafiklabProxy;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

public class TrafiklabProxyTest {

    private TrafiklabProxy subject;
    private EventBus eventBus;
    private JsonObject root;
    private JsonObject responseData;

    @Before
    public void setUp() {
        subject = new TrafiklabProxy();
        Vertx vertx = mock(Vertx.class);
        eventBus = mock(EventBus.class);
        when(vertx.eventBus()).thenReturn(eventBus);
        subject.setVertx(vertx);

        root = new JsonObject();
        responseData = new JsonObject();
        root.putObject("ResponseData", responseData);
    }

    @Test
    public void intercept() {
        Map<String, Object> trains = new LinkedHashMap<String, Object>() {{
            put("trains", asList(new LinkedHashMap<String, Object>() {{
                put("SiteId", 9525);
                put("StopAreaName", "Tullinge");
            }}));
        }};

        subject.intercept(new JsonObject(trains));

        verify(eventBus).send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
            put("9525", "Tullinge");
        }}));
    }

    @Test
    public void interceptWithNoTrains() {
        Map<String, Object> trains = new LinkedHashMap<>();

        subject.intercept(new JsonObject(trains));

        verify(eventBus, never()).send(anyString(), any(JsonObject.class));
    }

    @Test
    public void returnsJsonObjectWithTrainsInJsonArray() {
        array("Trains").add(departure());
        JsonObject result = TrafiklabFilter.invoke(root, null);
        assertEquals(1, result.getArray("trains").size());
    }

    @Test
    public void returnsBothBusesAndTrains() {
        array("Trains").add(departure());
        array("Buses").add(departure());

        JsonObject result = TrafiklabFilter.invoke(root, null);

        assertEquals(1, result.getArray("trains").size());
        assertEquals(1, result.getArray("buses").size());
    }

    @Test
    public void doesntCrashIfThereAreNoTrains() {
        array("Buses").add(departure());
        TrafiklabFilter.invoke(root, null);
    }

    @Test
    public void doesntCrashIfThereAreNonArrays() {
        array("Trains").add(departure());
        responseData.putNumber("DataAge", 19);
        TrafiklabFilter.invoke(root, null);
    }

    @Test
    public void skipsEmptyArrays() {
        array("Trains");
        array("Buses").add(departure());

        JsonObject result = TrafiklabFilter.invoke(root, null);

        assertFalse(result.getFieldNames().contains("trains"));
    }

    @Test
    public void returnsJsonObjectWithSiteIdAndStopAreaName() {
        array("Trains").add(departure(5181));

        JsonObject result = TrafiklabFilter.invoke(root, null);

        assertEquals(9525, result.getNumber("SiteId"));
        assertEquals("Tullinge", result.getString("StopAreaName"));
    }

    @Test
    public void trainStopAreaNumber() {
        array("Trains").add(departure(5181));
        array("Buses").add(departure(70243));

        JsonObject result = TrafiklabFilter.invoke(root, "5181");

        assertNotNull(result);
        assertEquals(1, result.getArray("trains").size());
        assertNull(result.getArray("buses"));
    }

    private JsonObject departure(final int stopAreaNumber) {
        return new JsonObject(new LinkedHashMap<String, Object>() {{
            put("SiteId", 9525);
            put("StopAreaName", "Tullinge");
            put("StopAreaNumber", stopAreaNumber);
        }});
    }

    private JsonArray array(String name) {
        JsonArray trains = new JsonArray();
        responseData.putArray(name, trains);
        return trains;
    }

    private JsonObject departure() {
        return new JsonObject();
    }
}