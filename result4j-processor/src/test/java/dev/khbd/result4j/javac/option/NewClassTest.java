package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class NewClassTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtArgumentPosition_propagate() throws Exception {
        String source =
                "package cases.new_class;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(int index) {\n" +
                "        var name = new Name(getName(index).unwrap(), getName(index).unwrap());\n" +
                "        return Option.some(name.name);\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "        return Option.some(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "\n" +
                "    static class Name {\n" +
                "        String name;\n" +
                "\n" +
                "        Name(String name1, String ignore) {\n" +
                "            this.name = name1;\n" +
                "        }\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/new_class/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.new_class.Main");
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
