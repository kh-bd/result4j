package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;

/**
 * @author Sergei_Khadanovich
 */
public class ConditionalExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInCondition_failCompilation() {
        String source =
                "package cases.conditional_expression;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return random().unwrap() ? Either.right(\"Alex\") : Either.right(\"Sergei\");\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Boolean> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Either.right(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInThen_failCompilation() {
        String source =
                "package cases.conditional_expression;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return condition() ? Either.right(nameInternal().unwrap().toUpperCase()) : Either.left(\"error\");\n" +
                "    }\n" +
                "\n" +
                "    public static boolean condition() {\n" +
                "        var rnd = new Random();\n" +
                "        return rnd.nextBoolean();\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, String> nameInternal() {\n" +
                "        return Either.right(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInElse_failCompilation() {
        String source =
                "package cases.conditional_expression;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return condition() ?  Option.none() : Either.right(nameInternal().unwrap().toUpperCase());\n" +
                "    }\n" +
                "\n" +
                "    public static boolean condition() {\n" +
                "        var rnd = new Random();\n" +
                "        return rnd.nextBoolean();\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, String> nameInternal() {\n" +
                "        return Either.right(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
