package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;

/**
 * @author Sergei_Khadanovich
 */
public class AssertTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInExpression() {
        String source =
                "package cases.assert_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        assert getName(flag).unwrap().equals(\"Alex\");\n" +
                "        returnTry.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/assert_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));

    }

    @Test
    public void propagate_unwrapCallInDetails() {
        String source =
                "package cases.assert_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        assert 1 != 2 : getName(flag).unwrap();\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/assert_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));

    }
}
