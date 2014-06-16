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
        root.putObject("DPS", dps);
    }

    @Test
    public void intercept() throws Exception {
        Map<String, Object> fromTrafiklab = new LinkedHashMap<String, Object>() {{
            put("SiteId", "9525");
            put("StopAreaName", "Tullinge");
        }};

        subject.intercept(new JsonArray(asList(fromTrafiklab)));

        verify(eventBus).send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
            put("9525", "Tullinge");
        }}));
    }

    @Test
    public void returnsEmptyArrayIfTrainsContainsNoDpsTrains() throws Exception {
        dps.putObject("Trains", new JsonObject());

        JsonArray result = subject.filterTrafiklabData(root);

        assertEquals(0, result.size());
    }
}