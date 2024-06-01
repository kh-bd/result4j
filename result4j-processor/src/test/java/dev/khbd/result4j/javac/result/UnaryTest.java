package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class UnaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inUnaryNegationExpression() throws Exception {
        String source = "package cases.in_unary;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName(boolean flag) {\n" +
                        "        var notFlag = !flag(flag).unwrap();\n" +
                        "\n" +
                        "        if (notFlag) {\n" +
                        "            return Result.error(\"error\");\n" +
                        "        }\n" +
                        "\n" +
                        "        return Result.success(\"Alex\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, Boolean> flag(boolean flag) {\n" +
                        "        return Result.success(flag);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_unary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_unary.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Result<String, String> name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with true
        name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("Alex");
    }

}
