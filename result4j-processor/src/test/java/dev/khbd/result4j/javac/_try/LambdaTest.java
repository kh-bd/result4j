package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class LambdaTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInLambdaBlock() throws Exception {
        String source =
                "package cases.in_lambda;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "import java.util.concurrent.Callable;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) throws Exception {\n" +
                "        Callable<Try<String>> call = () -> {\n" +
                "            var name = name(flag).unwrap();\n" +
                "            return Try.success(name.toUpperCase());\n" +
                "        };\n" +
                "        return call.call();\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> name(boolean flag) {\n" +
                "        return flag ? Try.success(\"Alex\") : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInLambdaExpression() throws Exception {
        String source =
                "package cases.in_lambda;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "import java.util.concurrent.Callable;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) throws Exception {\n" +
                "        Callable<Try<String>> call = () -> Try.success(name(flag).unwrap().toUpperCase());\n" +
                "        return call.call();\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> name(boolean flag) {\n" +
                "        return flag ? Try.success(\"Alex\") : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");
    }

}
