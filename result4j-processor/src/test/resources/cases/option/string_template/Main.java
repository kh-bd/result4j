package cases.string_template;

import dev.khbd.result4j.core.Option;

public class Main {

    public static Option<String> greet(boolean flag) {
        var greetings = STR."Hello \{getName(flag).unwrap()}";
        return Option.some(greetings);
    }

    private static Option<String> getName(boolean flag) {
        return flag ? Option.some("Alex") : Option.none();
    }
}
