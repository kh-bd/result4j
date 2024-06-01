package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ThrowTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInThrowExpression() throws Exception {
        String source = "package cases.throw_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        throw createThrow(flag).unwrap();\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, RuntimeException> createThrow(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(new RuntimeException());\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.throw_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with true
        Throwable error = catchThrowable(() -> method.invoke(null, true));
        assertThat(error).isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(RuntimeException.class);

//      invoke with false
        Result<String, ?> greet = (Result<String, ?>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledThrowExpression() {
        String source = "package cases.throw_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        label:\n" +
                        "        throw createThrow(flag).unwrap();\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, RuntimeException> createThrow(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(new RuntimeException());\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
