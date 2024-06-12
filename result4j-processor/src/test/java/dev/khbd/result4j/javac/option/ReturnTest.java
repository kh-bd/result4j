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
public class ReturnTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallOnFunctionArgument() throws Exception {
        String source = "package cases.in_return;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static <T> Option<T> __unwrap__(Option<T> value) {\n" +
                        "        return Option.some(value.unwrap());\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("__unwrap__", Option.class);

        // invoke with 0
        Option<String> unwrapped = (Option<String>) method.invoke(null, Option.none());
        assertThat(unwrapped.isEmpty()).isTrue();

        // invoke with negative
        unwrapped = (Option<String>) method.invoke(null, Option.some("Alex"));
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_unwrapCallInReturn_propagate() throws Exception {
        String source = "package cases.in_return;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> greet(int index) {\n" +
                        "        return Option.some(name(index).unwrap());\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Option<String> name(int index) {\n" +
                        "        if (index == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with negative
        greet = (Option<String>) method.invoke(null, -10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with positive
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInLabeledReturn_propagate() {
        String source = "package cases.in_return;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> greet(int index) {\n" +
                        "        label:\n" +
                        "        return Option.some(name(index).unwrap());\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Option<String> name(int index) {\n" +
                        "        if (index == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
