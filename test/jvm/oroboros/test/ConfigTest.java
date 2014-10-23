package oroboros.test;

import clojure.lang.Associative;
import org.junit.*;
import oroboros.Config;

import static org.junit.Assert.*;

public class ConfigTest {

    @Test
    public void standaloneConfiguration() {
        Associative config = Config.circle();
        config = config.assoc("x", "{{y}}");
        assertTrue(config.entryAt("x").val().equals("{{y}}"));
        config = config.assoc("y", "templated");
        assertTrue(config.entryAt("x").val().equals("templated"));
    }

}
