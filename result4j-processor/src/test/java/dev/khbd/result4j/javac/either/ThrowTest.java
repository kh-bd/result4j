package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Either;
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
        String source =
                "package cases.throw_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, ?> greet(boolean flag) {\n" +
                "        throw createThrow(flag).unwrap();\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, RuntimeException> createThrow(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(new RuntimeException());\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.throw_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with true
        Throwable error = catchThrowable(() -> method.invoke(null, true));
        assertThat(error).isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(RuntimeException.class);

//      invoke with false
        Either<String, ?> greet = (Either<String, ?>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledThrowExpression() {
        String source =
                "package cases.throw_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, ?> greet(boolean flag) {\n" +
                "        label:\n" +
                "        throw createThrow(flag).unwrap();\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, RuntimeException> createThrow(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(new RuntimeException());\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
