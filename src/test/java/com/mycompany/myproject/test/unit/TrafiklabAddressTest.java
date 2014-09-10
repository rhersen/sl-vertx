package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.TrafiklabAddress;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class TrafiklabAddressTest {

    private TrafiklabAddress subject;

    @Before
    public void setUp() throws Exception {
        subject = new TrafiklabAddress();
    }

    @Test
    public void shouldCreateUrlBasedOnSiteIdInRequestPath() throws Exception {
        String result = subject.getUrl("/departures/1111", "nyckel");
        assertTrue(result, result.matches(".+\\?key=nyckel.*siteId=1111$"));
    }

    @Test
    public void handlesTrailingSlash() throws Exception {
        String result = subject.getUrl("/departures/1111/", "nyckel");
        assertTrue(result, result.matches(".+\\?key=nyckel.*siteId=1111$"));
    }

    @Test
    public void usesFirstDigits() throws Exception {
        String result = subject.getUrl("/departures/1111/9999", "nyckel");
        assertTrue(result, result.matches(".+\\?key=nyckel.*siteId=1111$"));
    }

    @Test
    public void throwsOnBadSiteId() throws Exception {
        try {
            subject.getUrl("/departures/solna", "nyckel");
            fail("expected exception");
        } catch (RuntimeException e) {
            assertTrue(e.getMessage(), e.getMessage().contains("could not parse"));
        }
    }
}