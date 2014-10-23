package oroboros.test;

import org.junit.*;
import oroboros.Config;
import oroboros.Circle;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void testInMemory() {
        Circle config = Config.empty();

        config = config.set("x", "{{y}}");
        assertTrue(config.get("x").equals("{{y}}"));

        config = config.set("y", "templated");
        assertTrue(config.get("x").equals("templated"));
    }

    @Test
    public void testFromDisk() {
        Circle config = Config.load("./examples");
        assertTrue(config.get("simple", "name").equals("tom & jerry"));
    }

    @Test
    public void testOverride() {
        Circle config = Config.load("./examples", "jerry");
        assertEquals("jerry & tom", config.get("simple", "name"));

        config = config.set(Arrays.asList("simple", "cat"), "friends");
        assertEquals("jerry & friends", config.get("simple", "name"));
    }

}
