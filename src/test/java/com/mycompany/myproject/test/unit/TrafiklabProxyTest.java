package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.TrafiklabProxy;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.Vertx;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class TrafiklabProxyTest {

    private TrafiklabProxy subject;
    private EventBus eventBus;

    @Before
    public void setUp() {
        subject = new TrafiklabProxy();
        Vertx vertx = mock(Vertx.class);
        eventBus = mock(EventBus.class);
        when(vertx.eventBus()).thenReturn(eventBus);
        subject.setVertx(vertx);
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

}
