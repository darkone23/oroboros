package oroboros;

import java.util.Map;
import clojure.lang.Associative;
import clojure.lang.IFn;
import clojure.lang.RT;

public class Config {

    private static IFn stringify;
    private static IFn circlefn;
    private static IFn templatefn;
    private static IFn hashmap;

    static {
        stringify = loadClojureFn("clojure.walk", "stringify-keys");
        hashmap = loadClojureFn("clojure.core", "hash-map");
        circlefn = loadClojureFn("oroboros.core", "circle");
        templatefn = loadClojureFn("oroboros.core", "template-map");
    }

    public static Associative circle() {
        return (Associative) templatefn.invoke(hashmap.invoke());
    }

    public static Associative circle(String directory) {
        return circle(directory, null);
    }

    public static Associative circle(String directory, String config) {
        if (directory == null) {
            return javaify(circlefn.invoke(directory));
        } else {
            return javaify(circlefn.invoke(directory, config));
        }
    }

    private static Associative javaify(Object map) {
        return (Associative) templatefn.invoke(stringify.invoke(map));
    }

    private static IFn loadClojureFn(String namespace, String name) {
        try {
            clojure.lang.Compiler.eval(RT.readString("(require '" + namespace + ")"));
        } catch (Exception e) {}
        return (IFn) RT.var(namespace, name).deref();
    }
}
