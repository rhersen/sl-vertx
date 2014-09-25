package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.ParamsToJson;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;
import org.vertx.java.core.json.JsonObject;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class ParamsToJsonTest {

    private ParamsToJson subject;

    @Before
    public void setUp() {
        subject = new ParamsToJson();
    }

    @Test
    public void empty() {
        JsonObject result = subject.invoke(new CaseInsensitiveMultiMap());
        assertTrue(result.getFieldNames().isEmpty());
    }

    @SuppressWarnings("UnnecessaryBoxing")
    @Test
    public void allThreeParams() {
        CaseInsensitiveMultiMap params = new CaseInsensitiveMultiMap();
        params.add("longitude", "17.88");
        params.add("latitude", "59.22");
        params.add("limit", "16");

        JsonObject result = subject.invoke(params);

        assertEquals(Double.valueOf(17.88), result.getField("longitude"));
        assertEquals(Double.valueOf(59.22), result.getField("latitude"));
        assertEquals(Double.valueOf(16), result.getField("limit"));
    }
}
