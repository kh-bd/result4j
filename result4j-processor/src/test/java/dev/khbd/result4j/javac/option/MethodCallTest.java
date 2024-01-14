package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class MethodCallTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtReceiverPosition_propagate() throws Exception {
        String source =
                "package cases.method_call;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(int index) {\n" +
                "        var name = name(index).unwrap().toUpperCase();\n" +
                "        return Option.some(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "        return Option.some(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/method_call/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.method_call.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with negative
        greet = (Option<String>) method.invoke(null, -10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("ALEX");

        // invoke with positive
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("SERGEI");
    }

    @Test
    public void propagate_unwrapCallAtArgumentPosition_propagate() throws Exception {
        String source =
                "package cases.method_call;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(int index) {\n" +
                "        var result = Option.some(name(index).unwrap());\n" +
                "        return result;\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "        return Option.some(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/method_call/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.method_call.Main");
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
}
