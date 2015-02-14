package name.hersen.sl.test.unit;

import name.hersen.sl.Keys;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class KeysTest {
    @Test
    public void addsKeyToTrain() {
        assertEquals("21012", Keys.key("2015-02-11T21:01:00", 2));
        assertEquals("21012", Keys.key(9524, "2015-02-11T21:01:00", 2));
        assertEquals("21012", Keys.key(9529, "2015-02-11T21:01:00", 2));
    }

    @Test
    public void usesTimeAtStockholmS() {
        assertEquals("S18552", Keys.key(9528, "2015-02-11T18:46:00", 2));
        assertEquals("S18552", Keys.key(9525, "2015-02-11T18:37:00", 2));
    }

    @Test
    public void wrapsToNextHour() {
        assertEquals("S19102", Keys.key(9525, "2015-02-11T18:52:00", 2));
    }

    @Test
    public void doesntCrashIfStringDoesntMatch() {
        assertEquals("21:01:00", Keys.key("21:01:00", 2));
    }
}
