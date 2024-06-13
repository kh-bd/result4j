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
public class ConditionalExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_nestedThen_failCompilation() {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import java.util.Random;\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    static Random rnd = new Random();\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return rnd.nextBoolean()\n" +
                        "          ? (random().unwrap() ? Option.some(\"Alex\") : Option.none())\n" +
                        "          : Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> random() {\n" +
                        "        if (rnd.nextBoolean()) {\n" +
                        "            return Option.some(rnd.nextBoolean());\n" +
                        "        }\n" +
                        "        return Option.none();\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_nestedElse_failCompilation() {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import java.util.Random;\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    static Random rnd = new Random();\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return rnd.nextBoolean()\n" +
                        "          ? Option.some(\"Alex\")\n" +
                        "          : (random().unwrap() ? Option.some(\"Alex\") : Option.none());\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> random() {\n" +
                        "        if (rnd.nextBoolean()) {\n" +
                        "            return Option.some(rnd.nextBoolean());\n" +
                        "        }\n" +
                        "        return Option.none();\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_nestedCondition() throws Exception {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(int flag) {\n" +
                        "        return (flag(flag).unwrap() ? true : false)\n" +
                        "          ? Option.some(\"Alex\")\n" +
                        "          : Option.some(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> flag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Option<String> unwrapped = (Option<String>) method.invoke(null, 0);
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, 10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Option<String>) method.invoke(null, -10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInCondition() throws Exception {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(int flag) {\n" +
                        "        return flag(flag).unwrap()\n" +
                        "          ? Option.some(\"Alex\")\n" +
                        "          : Option.some(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> flag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Option<String> unwrapped = (Option<String>) method.invoke(null, 0);
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, 10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Option<String>) method.invoke(null, -10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInThen_failCompilation() {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import java.util.Random;\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return condition() ? Option.some(nameInternal().unwrap().toUpperCase()) : Option.none();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static boolean condition() {\n" +
                        "        var rnd = new Random();\n" +
                        "        return rnd.nextBoolean();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<String> nameInternal() {\n" +
                        "        return Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInElse_failCompilation() {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import java.util.Random;\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return condition() ?  Option.none() : Option.some(nameInternal().unwrap().toUpperCase());\n" +
                        "    }\n" +
                        "\n" +
                        "    public static boolean condition() {\n" +
                        "        var rnd = new Random();\n" +
                        "        return rnd.nextBoolean();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<String> nameInternal() {\n" +
                        "        return Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
