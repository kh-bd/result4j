package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class BinaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inBinaryOrBoth() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<?, Boolean> get(boolean flag1, boolean flag2) {
                        var result = getByFlag(flag1).unwrap() || getByFlag(flag2).unwrap();
                        return Result.success(result);
                    }
                
                    public static Result<?, Boolean> getByFlag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryOrRight() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<?, Boolean> get(boolean flag1, boolean flag2) {
                        var result = flag1 || getByFlag(flag2).unwrap();
                        return Result.success(result);
                    }
                
                    public static Result<?, Boolean> getByFlag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryOrLeft() throws Exception {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static <E> Result<E, Boolean> get(boolean flag1, boolean flag2) {
                        var result = Main.<E>getByFlag(flag1).unwrap() || flag2;
                        return Result.success(result);
                    }
                
                    public static <E> Result<E, Boolean> getByFlag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("get", boolean.class, boolean.class);

        // call with false
        Result<?, Boolean> unwrapped = (Result<?, Boolean>) method.invoke(null, false, false);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        unwrapped = (Result<?, Boolean>) method.invoke(null, true, false);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);

        unwrapped = (Result<?, Boolean>) method.invoke(null, false, true);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);

        // call with true
        unwrapped = (Result<?, Boolean>) method.invoke(null, true, true);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);
    }

    @Test
    public void propagate_inBinaryAndBoth() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<?, Boolean> get(boolean flag1, boolean flag2) {
                        var result = getByFlag(flag1).unwrap() && getByFlag(flag2).unwrap();
                        return Result.success(result);
                    }
                
                    public static Result<?, Boolean> getByFlag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryAndRight() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<?, Boolean> get(boolean flag1, boolean flag2) {
                        var result = flag1 && getByFlag(flag2).unwrap();
                        return Result.success(result);
                    }
                
                    public static Result<?, Boolean> getByFlag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryAndLeft() throws Exception {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static <E> Result<E, Boolean> get(boolean flag1, boolean flag2) {
                        var result = Main.<E>getByFlag(flag1).unwrap() && flag2;
                        return Result.success(result);
                    }
                
                    public static <E> Result<E, Boolean> getByFlag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("get", boolean.class, boolean.class);

        // call with false
        Result<?, Boolean> unwrapped = (Result<?, Boolean>) method.invoke(null, false, false);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        unwrapped = (Result<?, Boolean>) method.invoke(null, true, false);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        unwrapped = (Result<?, Boolean>) method.invoke(null, false, true);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        // call with true
        unwrapped = (Result<?, Boolean>) method.invoke(null, true, true);
        assertThat(unwrapped.isError()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);
    }


    @Test
    public void propagate_inBinaryBoth() throws Exception {
        String source = "package cases.in_binary;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, Integer> getSum(boolean flag1, boolean flag2) {\n" +
                        "        var result = getInt(flag1).unwrap() + getInt(flag2).unwrap();\n" +
                        "        return Result.success(result);\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Integer> getInt(boolean flag) {\n" +
                        "        return flag ? Result.success(10) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class, boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        sum = (Result<String, Integer>) method.invoke(null, true, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        sum = (Result<String, Integer>) method.invoke(null, false, true);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(20);
    }

    @Test
    public void propagate_inBinaryLeft() throws Exception {
        String source = "package cases.in_binary;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, Integer> getSum(boolean flag) {\n" +
                        "        var result = getInt(flag).unwrap() + 1;\n" +
                        "        return Result.success(result);\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Integer> getInt(boolean flag) {\n" +
                        "        return flag ? Result.success(10) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inBinaryRight() throws Exception {
        String source = "package cases.in_binary;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, Integer> getSum(boolean flag) {\n" +
                        "        var result = 1 + getInt(flag).unwrap();\n" +
                        "        return Result.success(result);\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Integer> getInt(boolean flag) {\n" +
                        "        return flag ? Result.success(10) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inComplexBinary() throws Exception {
        String source = "package cases.in_binary;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, Integer> getSum(boolean flag) {\n" +
                        "        var result = (getInt(flag).unwrap() + getInt(flag).unwrap()) + getInt(flag).unwrap();\n" +
                        "        return Result.success(result);\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Integer> getInt(boolean flag) {\n" +
                        "        return flag ? Result.success(10) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(30);
    }
}
