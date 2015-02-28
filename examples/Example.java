import oroboros.Config;

public class Example {
    public static void main(String[] args) {
        Config config = new Config()
            .set("name", "{{cat}} & {{mouse}}")
            .set("cat", "tom")
            .set("mouse", "jerry");
        assert config.get("name").equals("tom & jerry");
    }
}
