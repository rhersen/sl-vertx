package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.Interceptor;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.*;

public class InterceptorTest {

    private Interceptor subject;
    private EventBus eventBus;

    @Before
    public void setUp() {
        eventBus = mock(EventBus.class);
        subject = new Interceptor(eventBus);
    }

    @Test
    public void intercept() {
        Map<String, Object> trains = new LinkedHashMap<String, Object>() {{
            put("trains", asList(new LinkedHashMap<String, Object>() {{
                put("SiteId", 9525);
                put("StopAreaName", "Tullinge");
            }}));
        }};

        subject.invoke(new JsonObject(trains));

        verify(eventBus).send("store.put", new JsonObject(new LinkedHashMap<String, Object>() {{
            put("9525", "Tullinge");
        }}));
    }

    @Test
    public void interceptWithNoTrains() {
        Map<String, Object> trains = new LinkedHashMap<>();

        subject.invoke(new JsonObject(trains));

        verify(eventBus, never()).send(anyString(), any(JsonObject.class));
    }

}
