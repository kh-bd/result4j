package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class UnaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inUnaryNegationExpression() throws Exception {
        String source =
                "package cases.in_unary;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) {\n" +
                "        var notFlag = !flag(flag).unwrap();\n" +
                "\n" +
                "        if (notFlag) {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Boolean> flag(boolean flag) {\n" +
                "        return Try.success(flag);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_unary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_unary.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");
    }

}
