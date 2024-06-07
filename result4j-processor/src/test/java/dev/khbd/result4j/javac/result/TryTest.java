package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class TryTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInResourcesWithOneBlock() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "import java.lang.AutoCloseable;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        try (var name = getName(flag).unwrap()) {\n" +
                        "            return Result.success(name.name);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Name> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(new Name(\"Alex\"));\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "class Name implements AutoCloseable {\n" +
                        "    String name;\n" +
                        "\n" +
                        "    Name(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void close() {}\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInResourcesWithSeveralBlocksButUnwrapAtFirstPosition() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "import java.lang.AutoCloseable;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        try (var name = getName(flag).unwrap(); var name2 = new Name(\"Alex\")) {\n" +
                        "            return Result.success(name.name);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Name> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(new Name(\"Alex\"));\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "class Name implements AutoCloseable {\n" +
                        "    String name;\n" +
                        "\n" +
                        "    Name(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void close() {}\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInResourcesWithSeveralBlocksButUnwrapAtSecondPosition() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "import java.lang.AutoCloseable;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, ?> greet(boolean flag) {\n" +
                        "        try (var name = getName(flag).unwrap(); var name2 = getName(flag).unwrap()) {\n" +
                        "            return Result.success(name.name + name2.name);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Name> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(new Name(\"Alex\"));\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Name> getName(Name name) {\n" +
                        "        return Result.success(new Name(name.name));\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "class Name implements AutoCloseable {\n" +
                        "    String name;\n" +
                        "\n" +
                        "    Name(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void close() {}\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("AlexAlex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInResourcesWithSeveralVarAndNextVarUsesPrevVar() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "import java.lang.AutoCloseable;\n" +
                        "\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<?, String> greet(boolean flag) {\n" +
                        "        try (var name = getName(flag).unwrap(); var name2 = getName(name).unwrap(); var name3 = getName(name2).unwrap()) {\n" +
                        "            return Result.success(name.name + name2.name + name3.name);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Name> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(new Name(\"Alex\"));\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Name> getName(Name other) {\n" +
                        "        return Result.success(new Name(other.name));\n" +
                        "    }\n" +
                        "}\n" +
                        "\n" +
                        "class Name implements AutoCloseable {\n" +
                        "    String name;\n" +
                        "\n" +
                        "    Name(String name) {\n" +
                        "        this.name = name;\n" +
                        "    }\n" +
                        "\n" +
                        "    public void close() {}\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isError()).isFalse();
        assertThat(greet.get()).isEqualTo("AlexAlexAlex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInTryBlock() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> greet(boolean flag) {\n" +
                        "        try {\n" +
                        "            var name = getName(flag).unwrap();\n" +
                        "            return Result.success(name);\n" +
                        "        } catch (Exception e) {\n" +
                        "            e.printStackTrace();\n" +
                        "            return Result.error(\"error\");\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n" +
                        "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> greet(boolean flag) {\n" +
                        "        try {\n" +
                        "            throw new RuntimeException();\n" +
                        "        } catch (Exception e) {\n" +
                        "            return Result.success(getName(flag).unwrap());\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n" +
                        "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source = "package cases.try_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> greet(boolean flag) {\n" +
                        "        try {\n" +
                        "            throw new RuntimeException();\n" +
                        "        } finally {\n" +
                        "            var name = getName(flag).unwrap();\n" +
                        "            return Result.success(name);\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Result.success(\"Alex\");\n" +
                        "        }\n" +
                        "        return Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n" +
                        "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }
}
