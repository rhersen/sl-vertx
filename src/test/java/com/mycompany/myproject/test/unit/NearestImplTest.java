package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.NearestImpl;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

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
        assertNotNull(subject);
        assertEquals("Stockholms central", subject.get(59.3297573170160, 18.0579362297373).get(0));
        assertEquals("Märsta", subject.get(59.6276624202622, 17.8609502859119).get(0));
    }

    @Test
    public void nearLatitude() throws Exception {
        assertNotNull(subject);
        assertEquals("Märsta", subject.get(59.6, 17.96).get(0));
    }
}