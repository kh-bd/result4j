package cases.string_template;

import dev.khbd.result4j.core.Result;

public class Main {

    public static Result<Exception, String> greet(boolean flag) {
        var greetings = STR."Hello \{getName(flag).unwrap()}";
        return Result.success(greetings);
    }

    private static Result<Exception, String> getName(boolean flag) {
        return flag ? Result.success("Alex") : Result.error(new RuntimeException("error"));
    }
}
