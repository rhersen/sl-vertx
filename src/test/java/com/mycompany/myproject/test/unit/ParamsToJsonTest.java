package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.ParamsToJson;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.http.CaseInsensitiveMultiMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ParamsToJsonTest {

    private ParamsToJson subject;

    @Before
    public void setUp() {
        subject = new ParamsToJson();
    }

    @Test
    public void empty() {
        String result = subject.invoke(new CaseInsensitiveMultiMap());
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    public void allThreeParams() {
        CaseInsensitiveMultiMap params = new CaseInsensitiveMultiMap();
        params.add("longitude", "17.88");
        params.add("latitude", "59.22");
        params.add("limit", "16");

        String result = subject.invoke(params);

        assertEquals("59.22,17.88,16", result);
    }
}
