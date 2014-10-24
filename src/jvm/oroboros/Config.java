package oroboros;

import clojure.lang.Associative;
import clojure.lang.IFn;
import clojure.lang.RT;

import java.util.Arrays;
import java.util.List;

public class Config {

    final Associative circle;

    public Config() {
        this(Config.empty());
    }

    private Config(Associative circle) {
        this.circle = circle;
    }

    public Object get(Object... keys) {
        return Config.getIn(circle, Arrays.asList(keys));
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
        return new Config(circle.assoc(key, val));
    }

    public Config set(List keys, Object val) {
        return new Config(Config.assocIn(circle, keys, val));
    }

    public boolean has(Object... keys) {
        return get(keys) != null;
    }

    public Config overlay(Config other) {
        return new Config(Config.overlay(circle, other.circle));
    }

    public boolean equals(Object other) {
        return other instanceof Config && circle.equals(((Config) other).circle);
    }

    public String toString() {
        return circle.toString();
    }


    // static methods below

    private static IFn circlefn;
    private static IFn overlayfn;
    private static IFn templatefn;
    private static IFn hashmap;
    private static IFn getfn;
    private static IFn setfn;

    static {
        hashmap = clojureFn("clojure.core", "hash-map");
        getfn = clojureFn("clojure.core", "get-in");
        setfn = clojureFn("clojure.core", "assoc-in");
        circlefn = clojureFn("oroboros.core", "circle");
        overlayfn = clojureFn("oroboros.core", "overlay");
        templatefn = clojureFn("oroboros.core", "template-map");
        clojureFn("oroboros.core", "set-java-opts!").invoke();
    }

    public static Config load(String directory) {
        return Config.load(directory, null);
    }

    public static Config load(String directory, String config) {
        return (directory == null) ?
                new Config((Associative) circlefn.invoke(directory)) :
                new Config((Associative) circlefn.invoke(directory, config));
    }

    public static Config create() {
        return new Config();
    }

    private static Associative empty() {
        return (Associative) templatefn.invoke(hashmap.invoke());
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
            System.err.println("Failed to load " + namespace + "/" + name + ":" + e.getMessage());
        }
        return (IFn) RT.var(namespace, name).deref();
    }

}
