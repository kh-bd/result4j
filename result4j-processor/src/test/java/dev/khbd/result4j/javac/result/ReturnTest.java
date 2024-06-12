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
public class ReturnTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallOnFunctionArgument() throws Exception {
        String source = "package cases.in_return;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static <T> Result<String, T> __unwrap__(Result<String, T> value) {\n" +
                        "        return Result.success(value.unwrap());\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("__unwrap__", Result.class);

        // invoke with 0
        Result<String, String> unwrapped = (Result<String, String>) method.invoke(null, Result.error("error"));
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        // invoke with negative
        unwrapped = (Result<String, String>) method.invoke(null, Result.success("Alex"));
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_unwrapCallInReturn_propagate() throws Exception {
        String source = "package cases.in_return;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<RuntimeException, String> greet(int index) {\n" +
                        "        return Result.success(name(index).unwrap());\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<RuntimeException, String> name(int index) {\n" +
                        "        if (index == 0) {\n" +
                        "            return Result.error(new RuntimeException(\"error\"));\n" +
                        "        }\n" +
                        "        return Result.success(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Result<Exception, String> greet = (Result<Exception, String>) method.invoke(null, 0);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // invoke with negative
        greet = (Result<Exception, String>) method.invoke(null, -10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with positive
        greet = (Result<Exception, String>) method.invoke(null, 10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInLabeledReturn_propagate() {
        String source = "package cases.in_return;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<RuntimeException, String> greet(int index) {\n" +
                        "        label:\n" +
                        "        return Result.success(name(index).unwrap());\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<RuntimeException, String> name(int index) {\n" +
                        "        if (index == 0) {\n" +
                        "            return Result.error(new RuntimeException(\"error\"));\n" +
                        "        }\n" +
                        "        return Result.success(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
