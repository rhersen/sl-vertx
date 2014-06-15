package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.BodyHandler;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class BodyHandlerTest {

    private BodyHandler subject;
    private EventBus eventBus;

    @Before
    public void setUp() throws Exception {
        eventBus = mock(EventBus.class);
        subject = new BodyHandler(null, eventBus);
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
}