package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class CompoundAssignmentTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInAssignment() throws Exception {
        String source =
                "package cases.compound_assignment;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, Integer> getSize(boolean flag) {\n" +
                "        int result = 0;\n" +
                "\n" +
                "        result += baseSize(flag).unwrap();\n" +
                "\n" +
                "        return Either.right(result);\n" +
                "    }\n" +
                "\n" +
                "    private static Either<String, Integer> baseSize(boolean flag) {\n" +
                "        return flag ? Either.right(10) : Either.left(\"error\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/compound_assignment/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.compound_assignment.Main");
        Method method = clazz.getMethod("getSize", boolean.class);

        // invoke with true
        Either<String, Integer> greet = (Either<String, Integer>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo(10);

        // invoke with false
        greet = (Either<String, Integer>) method.invoke(null,false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }
}
