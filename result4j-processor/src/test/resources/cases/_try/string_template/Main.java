package cases.string_template;

import dev.khbd.result4j.core.Try;

public class Main {

    public static Try<String> greet(boolean flag) {
        var greetings = STR."Hello \{getName(flag).unwrap()}";
        return Try.success(greetings);
    }

    private static Try<String> getName(boolean flag) {
        return flag ? Try.success("Alex") : Try.failure(new RuntimeException("error"));
    }
}
