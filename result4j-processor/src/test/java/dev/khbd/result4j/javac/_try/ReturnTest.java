package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(int index) {\n" +
                "        return Try.success(name(index).unwrap());\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "        return Try.success(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Try<String> greet = (Try<String>) method.invoke(null, 0);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // invoke with negative
        greet = (Try<String>) method.invoke(null, -10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with positive
        greet = (Try<String>) method.invoke(null, 10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInLabeledReturn_propagate() {
        String source =
                "package cases.in_return;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(int index) {\n" +
                "        label:\n" +
                "        return Try.success(name(index).unwrap());\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "        return Try.success(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
