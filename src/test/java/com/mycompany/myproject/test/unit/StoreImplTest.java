package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.StoreImpl;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class StoreImplTest {

    private StoreImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new StoreImpl();
        subject.setStations(asList(
                9710, 9711, 9700, 9701, 9702, 9703, 9704, 9325,
                9502, 9503, 9504, 9505, 9506, 9507, 9508, 9509,
                9510, 9000, 9530, 9531, 9529,
                9528, 9527, 9526, 9525, 9524, 9523, 9522, 9521, 9520,
                9180, 9732, 9731, 9730, 9729, 9728, 9727, 9726, 9725));
    }

    @Test
    public void shouldReturnSiteIdAsStopAreaNameIfEmpty() throws Exception {
        JsonObject result = subject.get();
        assertEquals("9525", result.getArray("southwest").<JsonObject>get(3).getString("StopAreaName"));
    }

    @Test
    public void shouldReturnStopAreaNameIfItHasBeenPut() throws Exception {
        subject.put(new JsonObject(new LinkedHashMap<String, Object>() {{
            put("9525", "Tullinge");
        }}));

        JsonObject result = subject.get();
        assertEquals("Tullinge", result.getArray("southwest").<JsonObject>get(3).getString("StopAreaName"));
    }
}