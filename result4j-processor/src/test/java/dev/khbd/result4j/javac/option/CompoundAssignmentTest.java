package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<Integer> getSize(boolean flag) {\n" +
                "        int result = 0;\n" +
                "\n" +
                "        result += baseSize(flag).unwrap();\n" +
                "\n" +
                "        return Option.some(result);\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> baseSize(boolean flag) {\n" +
                "        return flag ? Option.some(10) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/compound_assignment/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.compound_assignment.Main");
        Method method = clazz.getMethod("getSize", boolean.class);

        // invoke with true
        Option<Integer> greet = (Option<Integer>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo(10);

        // invoke with false
        greet = (Option<Integer>) method.invoke(null,false);
        assertThat(greet.isEmpty()).isTrue();
    }
}
