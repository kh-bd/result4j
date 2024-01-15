package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class BinaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inBinaryBoth() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<Integer> getSum(boolean flag1, boolean flag2) {\n" +
                "        var result = getInt(flag1).unwrap() + getInt(flag2).unwrap();\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Integer> getInt(boolean flag) {\n" +
                "        return flag ? Try.success(10) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class, boolean.class);

        // call with false
        Try<Integer> sum = (Try<Integer>) method.invoke(null, false, false);
        assertThat(sum.isFailure()).isTrue();
        assertThat(sum.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        sum = (Try<Integer>) method.invoke(null, true, false);
        assertThat(sum.isFailure()).isTrue();
        assertThat(sum.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");


        sum = (Try<Integer>) method.invoke(null, false, true);
        assertThat(sum.isFailure()).isTrue();
        assertThat(sum.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");


        // call with true
        sum = (Try<Integer>) method.invoke(null, true, true);
        assertThat(sum.isFailure()).isFalse();
        assertThat(sum.get()).isEqualTo(20);
    }

    @Test
    public void propagate_inBinaryLeft() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<Integer> getSum(boolean flag) {\n" +
                "        var result = getInt(flag).unwrap() + 1;\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Integer> getInt(boolean flag) {\n" +
                "        return flag ? Try.success(10) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Try<Integer> sum = (Try<Integer>) method.invoke(null, false);
        assertThat(sum.isFailure()).isTrue();
        assertThat(sum.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with true
        sum = (Try<Integer>) method.invoke(null, true);
        assertThat(sum.isFailure()).isFalse();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inBinaryRight() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<Integer> getSum(boolean flag) {\n" +
                "        var result = 1 + getInt(flag).unwrap();\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Integer> getInt(boolean flag) {\n" +
                "        return flag ? Try.success(10) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Try<Integer> sum = (Try<Integer>) method.invoke(null, false);
        assertThat(sum.isFailure()).isTrue();
        assertThat(sum.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with true
        sum = (Try<Integer>) method.invoke(null, true);
        assertThat(sum.isFailure()).isFalse();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inComplexBinary() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<Integer> getSum(boolean flag) {\n" +
                "        var result = (getInt(flag).unwrap() + getInt(flag).unwrap()) + getInt(flag).unwrap();\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Integer> getInt(boolean flag) {\n" +
                "        return flag ? Try.success(10) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Try<Integer> sum = (Try<Integer>) method.invoke(null, false);
        assertThat(sum.isFailure()).isTrue();
        assertThat(sum.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with true
        sum = (Try<Integer>) method.invoke(null, true);
        assertThat(sum.isFailure()).isFalse();
        assertThat(sum.get()).isEqualTo(30);
    }
}
