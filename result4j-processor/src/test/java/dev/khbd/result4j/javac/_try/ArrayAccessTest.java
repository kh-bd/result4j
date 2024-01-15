package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(boolean flag) {\n" +
                "        var name = getArray(flag).unwrap()[0];\n" +
                "        return Try.success(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String[]> getArray(boolean flag) {\n" +
                "        return flag ? Try.success(new String[] {\"Alex\"}) : Try.failure(new RuntimeException());\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void propagate_unwrapCallAtIndexPosition() throws Exception {
        String source =
                "package cases.array_access;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(boolean flag) {\n" +
                "        var names = new String[] {\"Alex\"};\n" +
                "        var name = names[getIndex(flag).unwrap()];\n" +
                "        return Try.success(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Integer> getIndex(boolean flag) {\n" +
                "        return flag ? Try.success(0) : Try.failure(new RuntimeException());\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void propagate_unwrapCallAtInstanceAndIndexPositions() throws Exception {
        String source =
                "package cases.array_access;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(boolean flag) {\n" +
                "        var name = getArray(flag).unwrap()[getIndex(flag).unwrap()];\n" +
                "        return Try.success(name);\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String[]> getArray(boolean flag) {\n" +
                "        return flag ? Try.success(new String[] {\"Alex\"}) : Try.failure(new RuntimeException());\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Integer> getIndex(boolean flag) {\n" +
                "        return flag ? Try.success(0) : Try.failure(new RuntimeException());\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class);
    }
}
