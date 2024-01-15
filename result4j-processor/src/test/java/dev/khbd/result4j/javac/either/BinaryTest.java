package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, Integer> getSum(boolean flag1, boolean flag2) {\n" +
                "        var result = getInt(flag1).unwrap() + getInt(flag2).unwrap();\n" +
                "        return Either.right(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Integer> getInt(boolean flag) {\n" +
                "        return flag ? Either.right(10) : Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class, boolean.class);

        // call with false
        Either<String, Integer> sum = (Either<String, Integer>) method.invoke(null, false, false);
        assertThat(sum.isLeft()).isTrue();
        assertThat(sum.getLeft()).isEqualTo("error");

        sum = (Either<String, Integer>) method.invoke(null, true, false);
        assertThat(sum.isLeft()).isTrue();
        assertThat(sum.getLeft()).isEqualTo("error");

        sum = (Either<String, Integer>) method.invoke(null, false, true);
        assertThat(sum.isLeft()).isTrue();
        assertThat(sum.getLeft()).isEqualTo("error");

        // call with true
        sum = (Either<String, Integer>) method.invoke(null, true, true);
        assertThat(sum.isRight()).isTrue();
        assertThat(sum.getRight()).isEqualTo(20);
    }

    @Test
    public void propagate_inBinaryLeft() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, Integer> getSum(boolean flag) {\n" +
                "        var result = getInt(flag).unwrap() + 1;\n" +
                "        return Either.right(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Integer> getInt(boolean flag) {\n" +
                "        return flag ? Either.right(10) : Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Either<String, Integer> sum = (Either<String, Integer>) method.invoke(null, false);
        assertThat(sum.isLeft()).isTrue();
        assertThat(sum.getLeft()).isEqualTo("error");

        // call with true
        sum = (Either<String, Integer>) method.invoke(null, true);
        assertThat(sum.isRight()).isTrue();
        assertThat(sum.getRight()).isEqualTo(11);
    }

    @Test
    public void propagate_inBinaryRight() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, Integer> getSum(boolean flag) {\n" +
                "        var result = 1 + getInt(flag).unwrap();\n" +
                "        return Either.right(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Integer> getInt(boolean flag) {\n" +
                "        return flag ? Either.right(10) : Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Either<String, Integer> sum = (Either<String, Integer>) method.invoke(null, false);
        assertThat(sum.isLeft()).isTrue();
        assertThat(sum.getLeft()).isEqualTo("error");

        // call with true
        sum = (Either<String, Integer>) method.invoke(null, true);
        assertThat(sum.isRight()).isTrue();
        assertThat(sum.getRight()).isEqualTo(11);
    }

    @Test
    public void propagate_inComplexBinary() throws Exception {
        String source =
                "package cases.in_binary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, Integer> getSum(boolean flag) {\n" +
                "        var result = (getInt(flag).unwrap() + getInt(flag).unwrap()) + getInt(flag).unwrap();\n" +
                "        return Either.right(result);\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Integer> getInt(boolean flag) {\n" +
                "        return flag ? Either.right(10) : Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Either<String, Integer> sum = (Either<String, Integer>) method.invoke(null, false);
        assertThat(sum.isLeft()).isTrue();
        assertThat(sum.getLeft()).isEqualTo("error");

        // call with true
        sum = (Either<String, Integer>) method.invoke(null, true);
        assertThat(sum.isRight()).isTrue();
        assertThat(sum.getRight()).isEqualTo(30);
    }
}
