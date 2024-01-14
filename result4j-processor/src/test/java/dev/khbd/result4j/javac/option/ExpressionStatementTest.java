package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ExpressionStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallOnExpressionStatement_propagate() throws Exception {
        String source =
                "package cases.expression_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(int index) {\n" +
                "        // result is ignored\n" +
                "        name(index).unwrap();\n" +
                "        return Option.some(\"Alex\");\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "        return Option.some(\"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.expression_statement.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with not 0
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_unwrapCallOnLabeledExpressionStatement_fail() {
        String source =
                "package cases.expression_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(int index) {\n" +
                "        // result is ignored\n" +
                "        label:\n" +
                "        name(index).unwrap();\n" +
                "        return Option.some(\"Alex\");\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "        return Option.some(\"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
