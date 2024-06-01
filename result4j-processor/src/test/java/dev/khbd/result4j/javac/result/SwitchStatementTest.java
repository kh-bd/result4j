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
public class SwitchStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source = "package cases.switch_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        switch(toInteger(flag).unwrap()) {\n" +
                        "            case 1:\n" +
                        "            default:\n" +
                        "                return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Integer> toInteger(boolean flag) {\n" +
                        "        return flag ? Result.success(1) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source = "package cases.switch_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        label:\n" +
                        "        switch(toInteger(flag).unwrap()) {\n" +
                        "            case 1:\n" +
                        "            default:\n" +
                        "                return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Integer> toInteger(boolean flag) {\n" +
                        "        return flag ? Result.success(1) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/labeled/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithoutBlock() throws Exception {
        String source = "package cases.switch_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        switch(toInteger(flag)) {\n" +
                        "            case 1:\n" +
                        "                var name1 = getName(flag).unwrap();\n" +
                        "                return Result.success(name1);\n" +
                        "            default:\n" +
                        "                return Result.error(\"error\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "\n" +
                        "    private static int toInteger(boolean flag) {\n" +
                        "        return flag ? 1 : 0;\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInCaseStatementWithoutBlock() throws Exception {
        String source = "package cases.switch_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        switch(toInteger(flag)) {\n" +
                        "            case 1:\n" +
                        "                return Result.success(getName(flag).unwrap());\n" +
                        "            default:\n" +
                        "                return Result.error(\"error\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "\n" +
                        "    private static int toInteger(boolean flag) {\n" +
                        "        return flag ? 1 : 0;\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithBlock() throws Exception {
        String source = "package cases.switch_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        switch(toInteger(flag)) {\n" +
                        "            case 1: {\n" +
                        "                var name1 = getName(flag).unwrap();\n" +
                        "                return Result.success(name1);\n" +
                        "            }\n" +
                        "            default:\n" +
                        "                return Result.error(\"error\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "\n" +
                        "    private static int toInteger(boolean flag) {\n" +
                        "        return flag ? 1 : 0;\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }
}
