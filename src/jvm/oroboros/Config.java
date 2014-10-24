package oroboros;

import clojure.lang.Associative;
import clojure.lang.IFn;
import clojure.lang.RT;

import java.util.Arrays;
import java.util.List;

public class Config {

    final Associative config;

    public Config() {
        this(Config.empty());
    }

    private Config(Associative config) {
        this.config = config;
    }

    public Object get(Object... keys) {
        return Config.getIn(config, Arrays.asList(keys));
    }

    public String getStr(Object... keys) {
        return (String) get(keys);
    }

    public Integer getInt(Object... keys) {
        return (Integer) get(keys);
    }

    public Long getLong(Object... keys) {
        return (Long) get(keys);
    }

    public Boolean getBool(Object... keys) {
        return (Boolean) get(keys);
    }

    public Config set(Object key, Object val) {
        return new Config(config.assoc(key, val));
    }

    public Config set(List keys, Object val) {
        return new Config(Config.assocIn(config, keys, val));
    }

    public boolean has(Object... keys) {
        return get(keys) != null;
    }

    public Config overlay(Config other) {
        return new Config(Config.overlay(config, other.config));
    }

    public boolean equals(Object other) {
        return other instanceof Config && config.equals(((Config) other).config);
    }

    public String toString() {
        return config.toString();
    }

    // static methods below

    private static IFn getfn;
    private static IFn setfn;
    private static IFn loadfn;
    private static IFn configfn;
    private static IFn overlayfn;

    static {
        getfn = clojureFn("clojure.core", "get-in");
        setfn = clojureFn("clojure.core", "assoc-in");
        loadfn = clojureFn("oroboros.core", "load-config");
        configfn = clojureFn("oroboros.core", "config");
        overlayfn = clojureFn("oroboros.core", "overlay");
        clojureFn("oroboros.core", "set-java-opts!").invoke();
    }

    public static Config load(String directory) {
        return new Config((Associative) loadfn.invoke(directory));
    }

    public static Config load(String directory, String config) {
        return new Config((Associative) loadfn.invoke(directory, config));
    }

    public static Config create() {
        return new Config();
    }

    private static Associative empty() {
        return (Associative) configfn.invoke();
    }

    private static Object getIn(Associative obj, List keys) {
        return getfn.invoke(obj, keys);
    }

    private static Associative assocIn(Associative obj, List keys, Object val) {
        return (Associative) setfn.invoke(obj, keys, val);
    }

    private static Associative overlay(Associative a, Associative b) {
        return (Associative) overlayfn.invoke(a, b);
    }

    private static IFn clojureFn(String namespace, String name) {
        try {
            Object require = RT.readString("(require '" + namespace + ")");
            clojure.lang.Compiler.eval(require);
        } catch (Exception e) {
            System.err.println(String.format("Failed to load %s/%s - %s", namespace, name, e.getMessage()));
        }
        return (IFn) RT.var(namespace, name).deref();
    }

}
