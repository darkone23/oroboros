package oroboros;

import clojure.lang.Associative;
import clojure.lang.IFn;
import clojure.lang.RT;

import java.util.Arrays;
import java.util.List;

public class Config {

    final Associative __config;

    public Config() {
        this(Config.empty());
    }

    private Config(Associative config) {
        this.__config = config;
    }

    public Object get(Object... keys) {
        return Config.getIn(__config, Arrays.asList(keys));
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

    public List getList(Object... keys) {
        return (List) get(keys);
    }

    public Config getConf(Object... keys) {
        return new Config((Associative) get(keys));
    }

    public Boolean getBool(Object... keys) {
        return (Boolean) get(keys);
    }

    public Config set(Object key, Object val) {
        return new Config(__config.assoc(key, val));
    }

    public Config set(List keys, Object val) {
        return new Config(Config.assocIn(__config, keys, val));
    }


    public boolean has(Object... keys) {
        return get(keys) != null;
    }

    public String template(String templateString) {
        return Config.template(templateString, __config);
    }

    public Config overlay(Config other) {
        return new Config(Config.overlay(__config, other.__config));
    }

    public void write(String path) {
        Config.write(__config, path);
    }

    public boolean equals(Object other) {
        return other instanceof Config && __config.equals(((Config) other).__config);
    }

    public String toString() {
        return __config.toString();
    }

    public String toJson() {
        return Config.toJson(__config);
    }

    // static methods below

    private static IFn getfn;
    private static IFn setfn;
    private static IFn configfn;
    private static IFn templatefn;
    private static IFn fromjsonfn;
    private static IFn tojsonfn;
    private static IFn loadfn;
    private static IFn writefn;
    private static IFn overlayfn;

    static {
        setfn = clojureFn("clojure.core", "assoc-in");
        getfn = clojureFn("oroboros.core", "type-aware-get-in");
        configfn = clojureFn("oroboros.core", "config");
        templatefn = clojureFn("oroboros.core", "mustache");
        fromjsonfn = clojureFn("oroboros.core", "from-json");
        tojsonfn = clojureFn("oroboros.core", "to-json");
        loadfn = clojureFn("oroboros.core", "load-config");
        writefn = clojureFn("oroboros.core", "write-config");
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

    public static String template(String templateString, Associative config) {
        return (String) templatefn.invoke(templateString, config);
    }

    public static Config fromJson(String json) {
        return new Config((Associative) fromjsonfn.invoke(json));
    }

    public static String toJson(Associative config) {
        return (String) tojsonfn.invoke(config);
    }

    public static void write(Associative obj, String path) {
        writefn.invoke(obj, path);
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
