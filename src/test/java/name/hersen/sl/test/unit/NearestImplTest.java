package name.hersen.sl.test.unit;

import name.hersen.sl.NearestImpl;
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
        subject = new NearestImpl();

        subject.setStopPoints(asList(
                "#StopPointNumber;StopPointName;StopAreaNumber;LocationNorthingCoordinate;LocationEastingCoordinate;ZoneShortName;StopAreaTypeCode;LastModifiedUtcDateTime;ExistsFromDate",
                "05011;Stockholms central;5011;59.3297573170160;18.0579362297373;A;RAILWSTN;2012-06-23 00:00:00.000;2012-06-23 00:00:00.000",
                "05121;Märsta;5121;59.6276624202622;17.8609502859119;C;RAILWSTN;2012-06-23 00:00:00.000;2012-06-23 00:00:00.000",
                "throw me away"
        ).stream());

        subject.setSites(asList(
                "#SiteId;SiteName;StopAreaNumber;LastModifiedUtcDateTime;ExistsFromDate",
                "1002;Centralen;5011;2013-05-08 09:09:46.003;2013-05-09 00:00:00.000",
                "9500;Märsta;5121;2012-03-26 23:55:32.900;2012-06-23 00:00:00.000",
                "1009500;Märsta;5121;2012-03-26 23:55:32.900;2012-06-23 00:00:00.000"
        ).stream());
    }

    @Test
    public void same() throws Exception {
        JsonObject result1 = subject.get(json(59.3297573170160, 18.0579362297373)).<JsonObject>get(0);
        JsonObject result2 = subject.get(json(59.6276624202622, 17.8609502859119)).<JsonObject>get(0);

        assertEquals(0L, result1.getNumber("distance"));
        assertEquals(0L, result2.getNumber("distance"));
        assertEquals("9500", result2.getString("site"));
    }

    @Test
    public void fields() throws Exception {
        JsonObject result = subject.get(json(59.3297573170160, 18.0579362297373)).<JsonObject>get(0);

        assertEquals("Stockholms central", result.getString("name"));
        assertEquals("5011", result.getString("area"));
        assertEquals("1002", result.getString("site"));
        assertEquals(59.3297573170160, result.getNumber("latitude"));
        assertEquals(18.0579362297373, result.getNumber("longitude"));
    }

    @Test
    public void nearLatitude() throws Exception {
        assertEquals("Märsta", subject.get(json(59.6, 17.96)).<JsonObject>get(0).getString("name"));
        assertEquals(8226L, subject.get(json(59.6, 17.93)).<JsonObject>get(0).getNumber("distance"));
    }

    @Test
    public void nearLongitude() throws Exception {
        assertEquals("Stockholms central", subject.get(json(59.48, 18.05)).<JsonObject>get(0).getString("name"));
    }

    @Test
    public void manhattanDistanceIsNotGoodEnough() throws Exception {
        assertEquals("Märsta", subject.get(json(59.56, 18.05)).<JsonObject>get(0).getString("name"));
    }

    @Test
    public void returnsMoreThanOne() throws Exception {
        JsonArray result = subject.get(json(59.48, 18.05));
        assertEquals(2, result.size());
    }

    @Test
    public void returnsNoMoreThanLimit() throws Exception {
        JsonObject json = json(59.48, 18.05);
        json.putNumber("limit", 1);

        JsonArray result = subject.get(json);

        assertEquals(1, result.size());
    }

    @Test
    public void doesntCrash() throws Exception {
        NearestImpl uninitialized = new NearestImpl();
        uninitialized.get(json(59.48, 18.05));
    }

    private JsonObject json(double latitude, double longitude) {
        JsonObject r = new JsonObject();
        r.putNumber("latitude", latitude);
        r.putNumber("longitude", longitude);
        return r;
    }
}