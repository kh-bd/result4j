package dev.khbd.result4j.javac._try;

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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return random().unwrap() ? Try.success(\"Alex\") : Try.success(\"Sergei\");\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Boolean> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Try.success(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return condition() ? Try.success(nameInternal().unwrap().toUpperCase()) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    public static boolean condition() {\n" +
                "        var rnd = new Random();\n" +
                "        return rnd.nextBoolean();\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> nameInternal() {\n" +
                "        return Try.success(\"Alex\");\n" +
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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return condition() ?  Try.none() : Try.success(nameInternal().unwrap().toUpperCase());\n" +
                "    }\n" +
                "\n" +
                "    public static boolean condition() {\n" +
                "        var rnd = new Random();\n" +
                "        return rnd.nextBoolean();\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> nameInternal() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
