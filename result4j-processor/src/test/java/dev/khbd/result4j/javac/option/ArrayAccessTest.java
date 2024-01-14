package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ArrayAccessTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtInstancePosition() throws Exception {
        String source =
                "package cases.array_access;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(boolean flag) {\n" +
                "        var name = getArray(flag).unwrap()[0];\n" +
                "        return Option.some(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String[]> getArray(boolean flag) {\n" +
                "        return flag ? Option.some(new String[] {\"Alex\"}) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallAtIndexPosition() throws Exception {
        String source =
                "package cases.array_access;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(boolean flag) {\n" +
                "        var names = new String[] {\"Alex\"};\n" +
                "        var name = names[getIndex(flag).unwrap()];\n" +
                "        return Option.some(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> getIndex(boolean flag) {\n" +
                "        return flag ? Option.some(0) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallAtInstanceAndIndexPositions() throws Exception {
        String source =
                "package cases.array_access;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(boolean flag) {\n" +
                "        var name = getArray(flag).unwrap()[getIndex(flag).unwrap()];\n" +
                "        return Option.some(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String[]> getArray(boolean flag) {\n" +
                "        return flag ? Option.some(new String[] {\"Alex\"}) : Option.none();\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> getIndex(boolean flag) {\n" +
                "        return flag ? Option.some(0) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }
}
