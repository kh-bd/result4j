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
public class TryTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInResources_failToCompile() {
        String source =
                "package cases.try_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "import java.lang.AutoCloseable;\n" +
                "\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        try (var name = getName(flag).unwrap()) {\n" +
                "            return Try.success(name.name);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Name> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(new Name(\"Alex\"));\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
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

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInTryBlock() throws Exception {
        String source =
                "package cases.try_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(boolean flag) {\n" +
                "        try {\n" +
                "            var name = getName(flag).unwrap();\n" +
                "            return Try.success(name);\n" +
                "        } catch (Exception e) {\n" +
                "            e.printStackTrace();\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n" +
                "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source =
                "package cases.try_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(boolean flag) {\n" +
                "        try {\n" +
                "            throw new RuntimeException();\n" +
                "        } catch (Exception e) {\n" +
                "            return Try.success(getName(flag).unwrap());\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n" +
                "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source =
                "package cases.try_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> greet(boolean flag) {\n" +
                "        try {\n" +
                "            throw new RuntimeException();\n" +
                "        } finally {\n" +
                "            var name = getName(flag).unwrap();\n" +
                "            return Try.success(name);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n" +
                "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
