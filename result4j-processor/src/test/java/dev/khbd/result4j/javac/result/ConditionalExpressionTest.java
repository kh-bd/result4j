package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
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
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    static Random rnd = new Random();\n" +
                        "\n" +
                        "    public static Result<String, String> getName() {\n" +
                        "        return rnd.nextBoolean()\n" +
                        "          ? (random().unwrap() ? Result.success(\"Alex\") : Result.error(\"error\"))\n" +
                        "          : Result.success(\"Alex\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Boolean> random() {\n" +
                        "        if (rnd.nextBoolean()) {\n" +
                        "            return Result.success(rnd.nextBoolean());\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
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
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    static Random rnd = new Random();\n" +
                        "\n" +
                        "    public static Result<String, String> getName() {\n" +
                        "        return rnd.nextBoolean()\n" +
                        "          ? Result.success(\"Alex\")\n" +
                        "          : (random().unwrap() ? Result.success(\"Alex\") : Result.error(\"error\"));\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Boolean> random() {\n" +
                        "        if (rnd.nextBoolean()) {\n" +
                        "            return Result.success(rnd.nextBoolean());\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
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
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName(int flag) {\n" +
                        "        return (flag(flag).unwrap() ? true : false)\n" +
                        "          ? Result.success(\"Alex\")\n" +
                        "          : Result.success(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Boolean> flag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Result.error(\"error\");\n" +
                        "        }\n" +
                        "        return Result.success(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Result<String, String> unwrapped = (Result<String, String>) method.invoke(null, 0);
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<String, String>) method.invoke(null, 10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Result<String, String>) method.invoke(null, -10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInCondition() throws Exception {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName(int flag) {\n" +
                        "        return flag(flag).unwrap()\n" +
                        "          ? Result.success(\"Alex\")\n" +
                        "          : Result.success(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Boolean> flag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Result.error(\"error\");\n" +
                        "        }\n" +
                        "        return Result.success(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Result<String, String> unwrapped = (Result<String, String>) method.invoke(null, 0);
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<String, String>) method.invoke(null, 10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Result<String, String>) method.invoke(null, -10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInThen_failCompilation() {
        String source = "package cases.conditional_expression;\n" +
                        "\n" +
                        "import java.util.Random;\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName() {\n" +
                        "        return condition() ? Result.success(nameInternal().unwrap().toUpperCase()) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static boolean condition() {\n" +
                        "        var rnd = new Random();\n" +
                        "        return rnd.nextBoolean();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, String> nameInternal() {\n" +
                        "        return Result.success(\"Alex\");\n" +
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
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName() {\n" +
                        "        return condition() ?  Option.none() : Result.success(nameInternal().unwrap().toUpperCase());\n" +
                        "    }\n" +
                        "\n" +
                        "    public static boolean condition() {\n" +
                        "        var rnd = new Random();\n" +
                        "        return rnd.nextBoolean();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, String> nameInternal() {\n" +
                        "        return Result.success(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
