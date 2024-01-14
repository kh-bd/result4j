package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                "import dev.khbd.result4j.core.Option;\n" +
                "import java.util.concurrent.Callable;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> getName(boolean flag) throws Exception {\n" +
                "        Callable<Option<String>> call = () -> {\n" +
                "            var name = name(flag).unwrap();\n" +
                "            return Option.some(name.toUpperCase());\n" +
                "        };\n" +
                "        return call.call();\n" +
                "    }\n" +
                "\n" +
                "    public static Option<String> name(boolean flag) {\n" +
                "        return flag ? Option.some(\"Alex\") : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInLambdaExpression() throws Exception {
        String source =
                "package cases.in_lambda;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "import java.util.concurrent.Callable;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> getName(boolean flag) throws Exception {\n" +
                "        Callable<Option<String>> call = () -> Option.some(name(flag).unwrap().toUpperCase());\n" +
                "        return call.call();\n" +
                "    }\n" +
                "\n" +
                "    public static Option<String> name(boolean flag) {\n" +
                "        return flag ? Option.some(\"Alex\") : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");
    }

}
