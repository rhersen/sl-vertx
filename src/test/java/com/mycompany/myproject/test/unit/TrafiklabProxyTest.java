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
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("SiteId", "9525");
        map.put("StopAreaName", "Tullinge");
        JsonArray array = new JsonArray(asList(map));

        target.intercept(array);

        verify(eventBus).send("store.put", new JsonObject(map));
    }
}