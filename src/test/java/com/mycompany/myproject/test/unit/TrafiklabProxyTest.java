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
import static org.mockito.Mockito.*;

public class TrafiklabProxyTest {

    private TrafiklabProxy target;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {
        target = new TrafiklabProxy();
        Vertx vertx = mock(Vertx.class);
        eventBus = mock(EventBus.class);
        when(vertx.eventBus()).thenReturn(eventBus);
        target.setVertx(vertx);
    }

    @Test
    public void intercept() throws Exception {
        Map<String, Object> fromTrafiklab = new LinkedHashMap<String, Object>() {{
            put("SiteId", "9525");
            put("StopAreaName", "Tullinge");
        }};

        target.intercept(new JsonArray(asList(fromTrafiklab)));

        verify(eventBus).send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
            put("9525", "Tullinge");
        }}));
    }
}