package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class CompoundAssignmentTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInAssignment() throws Exception {
        String source = "package cases.compound_assignment;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, Integer> getSize(boolean flag) {\n" +
                        "        int result = 0;\n" +
                        "\n" +
                        "        result += baseSize(flag).unwrap();\n" +
                        "\n" +
                        "        return Result.success(result);\n" +
                        "    }\n" +
                        "\n" +
                        "    private static Result<String, Integer> baseSize(boolean flag) {\n" +
                        "        return flag ? Result.success(10) : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/compound_assignment/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.compound_assignment.Main");
        Method method = clazz.getMethod("getSize", boolean.class);

        // invoke with true
        Result<String, Integer> greet = (Result<String, Integer>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo(10);

        // invoke with false
        greet = (Result<String, Integer>) method.invoke(null,false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }
}
