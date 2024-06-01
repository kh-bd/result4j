package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class LambdaTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInLambdaBlock() throws Exception {
        String source = "package cases.in_lambda;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "import java.util.concurrent.Callable;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName(boolean flag) throws Exception {\n" +
                        "        Callable<Result<String, String>> call = () -> {\n" +
                        "            var name = name(flag).unwrap();\n" +
                        "            return Result.success(name.toUpperCase());\n" +
                        "        };\n" +
                        "        return call.call();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, String> name(boolean flag) {\n" +
                        "        return flag ? Result.success(\"Alex\") : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Result<String, String> name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with true
        name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInLambdaExpression() throws Exception {
        String source = "package cases.in_lambda;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Result;\n" +
                        "import java.util.concurrent.Callable;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Result<String, String> getName(boolean flag) throws Exception {\n" +
                        "        Callable<Result<String, String>> call = () -> Result.success(name(flag).unwrap().toUpperCase());\n" +
                        "        return call.call();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Result<String, String> name(boolean flag) {\n" +
                        "        return flag ? Result.success(\"Alex\") : Result.error(\"error\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Result<String, String> name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with true
        name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("ALEX");
    }

}
