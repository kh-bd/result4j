package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> getName(boolean flag) {\n" +
                "        var notFlag = !flag(flag).unwrap();\n" +
                "\n" +
                "        if (notFlag) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "\n" +
                "        return Option.some(\"Alex\");\n" +
                "    }\n" +
                "\n" +
                "    public static Option<Boolean> flag(boolean flag) {\n" +
                "        return Option.some(flag);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_unary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_unary.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");
    }

}
