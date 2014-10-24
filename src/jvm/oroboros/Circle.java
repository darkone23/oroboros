package oroboros;

import clojure.lang.Associative;

import java.util.Arrays;
import java.util.List;

public class Circle {
    private final Associative circle;

    public Circle(Associative circle) {
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

    public Circle set(Object key, Object val) {
        return new Circle(circle.assoc(key, val));
    }

    public Circle set(List keys, Object val) {
        return new Circle(Config.assocIn(circle, keys, val));
    }

    public boolean has(Object... keys) {
        return get(keys) != null;
    }

    public boolean equals(Object other) {
        return circle.equals(other);
    }

    public String toString() {
        return circle.toString();
    }

}
