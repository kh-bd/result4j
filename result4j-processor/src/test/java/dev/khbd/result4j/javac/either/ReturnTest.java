package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ReturnTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReturn_propagate() throws Exception {
        String source =
                "package cases.in_return;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<RuntimeException, String> greet(int index) {\n" +
                "        return Either.right(name(index).unwrap());\n" +
                "    }\n" +
                "\n" +
                "    private static Either<RuntimeException, String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Either.left(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "        return Either.right(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Either<Exception, String> greet = (Either<Exception, String>) method.invoke(null, 0);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // invoke with negative
        greet = (Either<Exception, String>) method.invoke(null, -10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with positive
        greet = (Either<Exception, String>) method.invoke(null, 10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInLabeledReturn_propagate() {
        String source =
                "package cases.in_return;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<RuntimeException, String> greet(int index) {\n" +
                "        label:\n" +
                "        return Either.right(name(index).unwrap());\n" +
                "    }\n" +
                "\n" +
                "    private static Either<RuntimeException, String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Either.left(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "        return Either.right(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
