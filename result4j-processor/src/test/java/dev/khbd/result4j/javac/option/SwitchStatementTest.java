package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class SwitchStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag).unwrap()) {\n" +
                "            case 1:\n" +
                "            default:\n" +
                "                return Option.some(\"Alex\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Option.some(1) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        label:\n" +
                "        switch(toInteger(flag).unwrap()) {\n" +
                "            case 1:\n" +
                "            default:\n" +
                "                return Option.some(\"Alex\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Option.some(1) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/labeled/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithoutBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1:\n" +
                "                var name1 = getName(flag).unwrap();\n" +
                "                return Option.some(name1);\n" +
                "            default:\n" +
                "                return Option.none();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Option.some(\"Alex\");\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInCaseStatementWithoutBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1:\n" +
                "                return Option.some(getName(flag).unwrap());\n" +
                "            default:\n" +
                "                return Option.none();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Option.some(\"Alex\");\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1: {\n" +
                "                var name1 = getName(flag).unwrap();\n" +
                "                return Option.some(name1);\n" +
                "            }\n" +
                "            default:\n" +
                "                return Option.none();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Option.some(\"Alex\");\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }
}
