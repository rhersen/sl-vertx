package com.mycompany.myproject.test.unit;

import com.mycompany.myproject.ResponseStatus;
import org.junit.Before;
import org.junit.Test;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedHashMap;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ResponseStatusTest {

    private ResponseStatus subject;

    @Before
    public void setUp() {
    }

    @Test
    public void statusZero() {
        subject = ResponseStatus.valueOf(new JsonObject(new LinkedHashMap<String, Object>() {{
            put("StatusCode", 0);
        }}));

        assertTrue(subject.isOk);
        assertFalse(subject.isThrottled);
    }

    @Test
    public void throttled() {
        subject = ResponseStatus.valueOf(new JsonObject(new LinkedHashMap<String, Object>() {{
            put("StatusCode", 1006);
        }}));

        assertFalse(subject.isOk);
        assertTrue(subject.isThrottled);
    }

    @Test
    public void noStatus() {
        subject = ResponseStatus.valueOf(new JsonObject(new LinkedHashMap<String, Object>() {{
            put("Message", "It's probably OK");
        }}));

        assertTrue(subject.isOk);
        assertFalse(subject.isThrottled);
    }

    @Test
    public void unexpectedStatus() {
        subject = ResponseStatus.valueOf(new JsonObject(new LinkedHashMap<String, Object>() {{
            put("StatusCode", 666);
        }}));

        assertFalse(subject.isOk);
        assertFalse(subject.isThrottled);
    }
}
