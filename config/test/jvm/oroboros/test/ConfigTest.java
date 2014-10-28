package oroboros.test;

import org.junit.*;
import oroboros.Config;

import java.util.Arrays;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void testInMemory() {
        Config config = new Config();
        config = config.set("name", "{{mouse}}");
        assertEquals("{{mouse}}", config.get("name"));
        config = config.set("mouse", "jerry");
        assertEquals("jerry", config.get("name"));
    }

    @Test
    public void testFromDisk() {
        Config config = Config.load("../examples");
        assertEquals("tom & jerry", config.get("simple", "name"));
    }

    @Test
    public void testOverride() {
        Config config = Config.load("../examples", "jerry");
        assertEquals("jerry & tom", config.get("simple", "name"));
        config = config.set(Arrays.asList("simple", "cat"), "cheese");
        assertEquals("jerry & cheese", config.get("simple", "name"));
    }

    @Test
    public void testTemplate() {
        Config config = new Config().set("cat", "tom");
        config = config.set("mouse", "jerry");
        assertEquals("tom & jerry", config.template("{{ cat }} & {{ mouse }}"));
    }


    @Test
    public void testOverlay() {
        Config config = new Config().set("cat", "{{name}}");
        Config other = new Config().set("name", "tom");
        Config mixed = config.overlay(other);
        assertEquals("tom", mixed.get("cat"));
        assertFalse(mixed.has("name"));
    }

    @Test
    public void testEquals() {
        Config a = new Config().set("a", "{{ b }}").set("b", "a");
        Config b = new Config().set("b", "{{ a }}").set("a", "a");
        assertEquals(a, b);
    }

    @Test
    public void testHas() {
        Config config = Config.load("../examples");
        assertTrue(config.has("simple", "cat"));
        assertFalse(config.has("missing", "key"));
    }

    @Test
    public void testGetTyped() {
        assertTrue(new Config().set("str", "str").getStr("str") instanceof String);
        assertTrue(new Config().set("int", 1).getInt("int") instanceof Integer);
        assertTrue(new Config().set("long", 100L).getLong("long") instanceof Long);
        assertTrue(new Config().set("bool", true).getBool("bool") instanceof Boolean);
    }

    @Test
    public void testJson() {
        Config config = new Config().set("cat", "tom").set("mouse", "jerry").set("name", "{{cat}} & {{mouse}}");
        String json = "{\"name\":\"{{cat}} & {{mouse}}\",\"mouse\":\"jerry\",\"cat\":\"tom\"}";
        assertEquals(Config.fromJson(json), config);
        assertEquals(config.toJson(), Config.fromJson(json).toJson());
    }

}
