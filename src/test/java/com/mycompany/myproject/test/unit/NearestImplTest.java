package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.NearestImpl;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;

public class NearestImplTest {

    private NearestImpl subject;

    @Before
    public void setUp() throws Exception {
        subject = new NearestImpl(asList(
                "05011;Stockholms central;5011;59.3297573170160;18.0579362297373;A;RAILWSTN;2012-06-23 00:00:00.000;2012-06-23 00:00:00.000",
                "05121;Märsta;5121;59.6276624202622;17.8609502859119;C;RAILWSTN;2012-06-23 00:00:00.000;2012-06-23 00:00:00.000"
        ).stream());
    }

    @Test
    public void same() throws Exception {
        JsonObject result1 = subject.get(59.3297573170160, 18.0579362297373).<JsonObject>get(0);
        JsonObject result2 = subject.get(59.6276624202622, 17.8609502859119).<JsonObject>get(0);

        assertEquals(0L, result1.getNumber("distance"));
        assertEquals(0L, result2.getNumber("distance"));
    }

    @Test
    public void fields() throws Exception {
        JsonObject result = subject.get(59.3297573170160, 18.0579362297373).<JsonObject>get(0);

        assertEquals("Stockholms central", result.getString("name"));
        assertEquals("5011", result.getString("area"));
    }

    @Test
    public void nearLatitude() throws Exception {
        assertEquals("Märsta", subject.get(59.6, 17.96).<JsonObject>get(0).getString("name"));
        assertEquals(8226L, subject.get(59.6, 17.93).<JsonObject>get(0).getNumber("distance"));
    }

    @Test
    public void nearLongitude() throws Exception {
        assertEquals("Stockholms central", subject.get(59.48, 18.05).<JsonObject>get(0).getString("name"));
    }

    @Test
    public void manhattanDistanceIsNotGoodEnough() throws Exception {
        assertEquals("Märsta", subject.get(59.56, 18.05).<JsonObject>get(0).getString("name"));
    }

    @Test
    public void returnsMoreThanOne() throws Exception {
        JsonArray result = subject.get(59.48, 18.05);
        assertEquals(2, result.size());
    }
}