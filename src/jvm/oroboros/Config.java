package oroboros;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import clojure.lang.Associative;
import clojure.lang.IFn;
import clojure.lang.RT;

public class Config {

    private static IFn keyfn;
    private static IFn circlefn;
    private static IFn overlayfn;
    private static IFn templatefn;
    private static IFn hashmap;
    private static IFn getfn;
    private static IFn setfn;

    static {
        keyfn = loadClojureFn("clojure.core", "keyword");
        hashmap = loadClojureFn("clojure.core", "hash-map");
        getfn = loadClojureFn("clojure.core", "get-in");
        setfn = loadClojureFn("clojure.core", "assoc-in");
        circlefn = loadClojureFn("oroboros.core", "circle");
        overlayfn = loadClojureFn("oroboros.core", "overlay");
        templatefn = loadClojureFn("oroboros.core", "template-map");
        loadClojureFn("oroboros.core", "set-java-opts!").invoke();
    }

    public static Circle empty() {
        return circle(templatefn.invoke(hashmap.invoke()));
    }

    public static Circle load(String directory) {
        return load(directory, null);
    }

    public static Circle load(String directory, String config) {
        if (directory == null) {
            return circle(circlefn.invoke(directory));
        } else {
            return circle(circlefn.invoke(directory, config));
        }
    }

    public static Object getIn(Associative obj, List keys) {
        return getfn.invoke(obj, keys);
    }

    public static Associative assocIn(Associative obj, List keys, Object val) {
        return (Associative) setfn.invoke(obj, keys, val);
    }

    public static Circle overlay(Associative a, Associative b) {
        return circle(overlayfn.invoke(a, b));
    }

    private static Circle circle(Object assoc) {
        return new Circle((Associative) assoc);
    }

    private static IFn loadClojureFn(String namespace, String name) {
        try {
            Object require = RT.readString("(require '" + namespace + ")");
            clojure.lang.Compiler.eval(require);
        } catch (Exception e) {
            System.err.println("Failed to load " + namespace + "/" + name + ":" + e.getMessage());
        }
        return (IFn) RT.var(namespace, name).deref();
    }
}
