import oroboros.Config;
import java.util.Map;
import clojure.lang.Associative;

public class Example {
    public static void main(String[] args) {
        Associative config = Config.circle();
        config = config.assoc("message", "the var is: {{ var }}");
        config = config.assoc("var", "wow!");
        assert config.entryAt("message").val().equals("the var is: wow!");
    }
}
