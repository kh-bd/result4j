package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
                "import dev.khbd.result4j.core.Either;\n" +
                "import java.lang.AutoCloseable;\n" +
                "\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, ?> greet(boolean flag) {\n" +
                "        try (var name = getName(flag).unwrap()) {\n" +
                "            return Either.right(name.name);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, Name> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(new Name(\"Alex\"));\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
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
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> greet(boolean flag) {\n" +
                "        try {\n" +
                "            var name = getName(flag).unwrap();\n" +
                "            return Either.right(name);\n" +
                "        } catch (Exception e) {\n" +
                "            e.printStackTrace();\n" +
                "            return Either.left(\"error\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(\"Alex\");\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
                "    }\n" +
                "}\n" +
                "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source =
                "package cases.try_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> greet(boolean flag) {\n" +
                "        try {\n" +
                "            throw new RuntimeException();\n" +
                "        } catch (Exception e) {\n" +
                "            return Either.right(getName(flag).unwrap());\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(\"Alex\");\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
                "    }\n" +
                "}\n" +
                "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source =
                "package cases.try_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> greet(boolean flag) {\n" +
                "        try {\n" +
                "            throw new RuntimeException();\n" +
                "        } finally {\n" +
                "            var name = getName(flag).unwrap();\n" +
                "            return Either.right(name);\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(\"Alex\");\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
                "    }\n" +
                "}\n" +
                "\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }
}
