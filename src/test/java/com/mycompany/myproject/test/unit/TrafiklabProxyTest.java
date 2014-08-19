package com.mycompany.myproject.test.unit;

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
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class TrafiklabProxyTest {

    private TrafiklabProxy subject;
    private EventBus eventBus;
    private JsonObject root;
    private JsonObject dps;

    @Before
    public void setUp() throws Exception {
        subject = new TrafiklabProxy();
        Vertx vertx = mock(Vertx.class);
        eventBus = mock(EventBus.class);
        when(vertx.eventBus()).thenReturn(eventBus);
        subject.setVertx(vertx);

        root = new JsonObject();
        dps = new JsonObject();
        root.putObject("ResponseData", dps);
    }

    @Test
    public void intercept() throws Exception {
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
    public void returnsEmptyArrayIfTrainsContainsNoDpsTrains() throws Exception {
        dps.putArray("Trains", new JsonArray());

        JsonObject result = subject.filterTrafiklabData(root);

        assertEquals(0, result.getArray("trains").size());
    }

    @Test
    public void returnsJsonObjectWithTrainsInJsonArray() throws Exception {
        JsonObject dpsTrain = new JsonObject();
        getDpsTrains().add(dpsTrain);

        JsonObject result = subject.filterTrafiklabData(root);

        assertEquals(1, result.getArray("trains").size());
    }

    @Test
    public void returnsJsonObjectWithSiteIdAndStopAreaName() throws Exception {
        JsonObject dpsTrain = new JsonObject(new LinkedHashMap<String, Object>() {{
            put("SiteId", 9525);
            put("StopAreaName", "Tullinge");
        }});
        getDpsTrains().add(dpsTrain);

        JsonObject result = subject.filterTrafiklabData(root);

        assertEquals(9525, result.getNumber("SiteId"));
        assertEquals("Tullinge", result.getString("StopAreaName"));
    }

    private JsonArray getDpsTrains() {
        JsonArray dpsTrains = new JsonArray();
        dps.putArray("Trains", dpsTrains);
        return dpsTrains;
    }
}