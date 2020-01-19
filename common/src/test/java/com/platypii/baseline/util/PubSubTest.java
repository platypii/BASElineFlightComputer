package com.platypii.baseline.util;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Ensure that we are PubSubing correctly
 */
public class PubSubTest {

    @Test
    public void subscribe() {
        final PubSub<String> events = new PubSub<>();
        events.subscribe((msg) -> assertEquals("BASE", msg));
        events.post("BASE");
    }

    @Test
    public void subscribeMain() {
        final PubSub<String> events = new PubSub<>();
        events.subscribeMain((msg) -> assertEquals("BASE", msg));
        events.post("BASE");
    }

    @Test
    public void unsubscribe() {
        final PubSub<String> events = new PubSub<>();
        PubSub.Subscriber<String> listener = (msg) -> fail();
        events.subscribe(listener);
        events.unsubscribe(listener);
        events.post("BASE");
    }

    @Test
    public void unsubscribeMain() {
        final PubSub<String> events = new PubSub<>();
        PubSub.Subscriber<String> listener = (msg) -> fail();
        events.subscribeMain(listener);
        events.unsubscribeMain(listener);
        events.post("BASE");
    }

}
