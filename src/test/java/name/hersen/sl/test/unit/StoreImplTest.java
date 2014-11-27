package name.hersen.sl.test.unit;

import name.hersen.sl.StoreImpl;
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
        subject.putStations("northwest", asList(9710, 9711));
        subject.putStations("northeast", asList(9502, 9503));
        subject.putStations("central", asList(9510, 9000));
        subject.putStations("southwest", asList(9528, 9527, 9526, 9525, 9524, 9523, 9522, 9521, 9520));
        subject.putStations("southeast", asList(9180, 9732));
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