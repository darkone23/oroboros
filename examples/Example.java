import oroboros.Config;

public class Example {
    public static void main(String[] args) {
        Config config = new Config();
        config = config.set("message", "the var is: {{ var }}");
        config = config.set("var", "wow!");
        assert config.get("message").equals("the var is: wow!");
    }
}