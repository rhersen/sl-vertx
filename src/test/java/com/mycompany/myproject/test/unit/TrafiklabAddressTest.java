package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.TrafiklabAddress;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class TrafiklabAddressTest {

    private TrafiklabAddress subject;

    @Before
    public void setUp() throws Exception {
        subject = new TrafiklabAddress();
    }

    @Test
    public void shouldCreateUrlBasedOnSiteIdInRequestPath() throws Exception {
        String result = subject.getUrl("/departures/1111", "nyckel");
        assertTrue(result.matches(".+\\?key=nyckel&timeWindow=60&siteId=1111$"));
    }
}