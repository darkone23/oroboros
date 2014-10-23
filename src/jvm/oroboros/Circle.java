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

    public Circle set(Object key, Object val) {
        return new Circle(circle.assoc(key, val));
    }

    public Circle set(List keys, Object val) {
        return new Circle(Config.assocIn(circle, keys, val));
    }

    public boolean equals(Object other) {
        return circle.equals(other);
    }

    public String toString() {
        return circle.toString();
    }

}
