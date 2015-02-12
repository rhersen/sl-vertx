package name.hersen.sl.test.unit;

import name.hersen.sl.TrafiklabFilter;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;

import static org.junit.Assert.*;

public class TrafiklabFilterTest {
    private JsonObject root;
    private JsonObject responseData;

    @Before
    public void setUp() {
        root = new JsonObject();
        responseData = new JsonObject();
        root.putObject("ResponseData", responseData);
    }

    @Test
    public void returnsJsonObjectWithTrainsInJsonArray() {
        array("Trains").add(departure());
        JsonObject result = TrafiklabFilter.invoke(root, null);
        assertEquals(1, result.getArray("trains").size());
    }

    @Test
    public void returnsBothBusesAndTrains() {
        array("Trains").add(departure());
        array("Buses").add(departure());

        JsonObject result = TrafiklabFilter.invoke(root, null);

        assertEquals(1, result.getArray("trains").size());
        assertEquals(1, result.getArray("buses").size());
    }

    @Test
    public void doesntCrashIfThereAreNoTrains() {
        array("Buses").add(departure());
        TrafiklabFilter.invoke(root, null);
    }

    @Test
    public void doesntCrashIfThereAreNonArrays() {
        array("Trains").add(departure());
        responseData.putNumber("DataAge", 19);
        TrafiklabFilter.invoke(root, null);
    }

    @Test
    public void skipsEmptyArrays() {
        array("Trains");
        array("Buses").add(departure());

        JsonObject result = TrafiklabFilter.invoke(root, null);

        assertFalse(result.getFieldNames().contains("trains"));
    }

    @Test
    public void returnsJsonObjectWithSiteIdAndStopAreaName() {
        array("Trains").add(departure(5181));

        JsonObject result = TrafiklabFilter.invoke(root, null);

        assertEquals(9525, result.getNumber("SiteId"));
        assertEquals("Tullinge", result.getString("StopAreaName"));
    }

    @Test
    public void addsKeyToTrain() {
        array("Trains").add(departure(5181, "2015-02-11T21:01:00"));
        JsonObject result = TrafiklabFilter.invoke(root, null);
        assertEquals("21012", result.getArray("trains").<JsonObject>get(0).getString("Key"));
    }

    @Test
    public void doesntCrashIfStringDoesntMatch() {
        array("Trains").add(departure(5181, "21:01:00"));
        JsonObject result = TrafiklabFilter.invoke(root, null);
        assertEquals("21:01:00", result.getArray("trains").<JsonObject>get(0).getString("Key"));
    }

    @Test
    public void throwsOnDuplicateKeys() {
        JsonArray trains = array("Trains");
        trains.add(departure(5181, "21:01:00"));
        trains.add(departure(5181, "21:01:00"));
        try {
            TrafiklabFilter.invoke(root, null);
            fail("expected exception");
        } catch (Exception e) {
            // expected exception
        }
    }

    @Test
    public void trainStopAreaNumber() {
        array("Trains").add(departure(5181));
        array("Buses").add(departure(70243));

        JsonObject result = TrafiklabFilter.invoke(root, "5181");

        assertNotNull(result);
        assertEquals(1, result.getArray("trains").size());
        assertNull(result.getArray("buses"));
    }

    @Test
    public void dontRemoveDeparturesWithoutStopAreaNumber() {
        array("Trains").add(departure(-1));
        array("Buses").add(departure(70243));

        JsonObject result = TrafiklabFilter.invoke(root, "5181");

        assertNotNull(result);
        assertEquals(1, result.getArray("trains").size());
        assertNull(result.getArray("buses"));
    }

    private JsonObject departure(final int stopAreaNumber) {
        return departure(stopAreaNumber, null);
    }

    private JsonObject departure(final int stopAreaNumber, final String timeTabledDateTime) {
        return new JsonObject(new LinkedHashMap<String, Object>() {{
            put("SiteId", 9525);
            put("StopAreaName", "Tullinge");
            if (timeTabledDateTime != null) {
                put("TimeTabledDateTime", timeTabledDateTime);
            }
            put("JourneyDirection", 2);
            if (stopAreaNumber > 0) {
                put("StopAreaNumber", stopAreaNumber);
            }
        }});
    }

    private JsonArray array(String name) {
        JsonArray trains = new JsonArray();
        responseData.putArray(name, trains);
        return trains;
    }

    private JsonObject departure() {
        return new JsonObject();
    }
}
