import oroboros.Config;

public class Example {
    public static void main(String[] args) {
        Config config = new Config();
        config = config.set("name", "{{cat}} & {{mouse}}");
        config = config.set("cat", "tom");
        config = config.set("mouse", "jerry");
        assert config.get("name").equals("tom & jerry");
    }
}