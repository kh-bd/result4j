package cases.string_template;

import dev.khbd.result4j.core.Either;

public class Main {

    public static Either<Exception, String> greet(boolean flag) {
        var greetings = STR."Hello \{getName(flag).unwrap()}";
        return Either.right(greetings);
    }

    private static Either<Exception, String> getName(boolean flag) {
        return flag ? Either.right("Alex") : Either.left(new RuntimeException("error"));
    }
}
