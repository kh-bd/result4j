package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Try;
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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag).unwrap()) {\n" +
                "            case 1:\n" +
                "            default:\n" +
                "                return Try.success(\"Alex\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Try.success(1) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        label:\n" +
                "        switch(toInteger(flag).unwrap()) {\n" +
                "            case 1:\n" +
                "            default:\n" +
                "                return Try.success(\"Alex\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Try.success(1) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/labeled/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInRuleWithBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1 -> {\n" +
                "                var name = getName(flag).unwrap();\n" +
                "                return Try.success(name);\n" +
                "            }\n" +
                "            default -> {\n" +
                "                return Try.failure(new RuntimeException(\"error\"));\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInRuleWithOneStatement() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1 -> throw error(flag).unwrap();\n" +
                "            default -> {\n" +
                "                return Try.failure(new RuntimeException(\"error\"));\n" +
                "            }\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<RuntimeException> error(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(new RuntimeException(\"Alex\"));\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Throwable error = catchThrowable(() -> method.invoke(null, true));
        assertThat(error)
                .cause()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Alex");

        // invoke with false
        Try<String> greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithoutBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1:\n" +
                "                var name1 = getName(flag).unwrap();\n" +
                "                return Try.success(name1);\n" +
                "            default:\n" +
                "                return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInCaseStatementWithoutBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1:\n" +
                "                return Try.success(getName(flag).unwrap());\n" +
                "            default:\n" +
                "                return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithBlock() throws Exception {
        String source =
                "package cases.switch_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        switch(toInteger(flag)) {\n" +
                "            case 1: {\n" +
                "                var name1 = getName(flag).unwrap();\n" +
                "                return Try.success(name1);\n" +
                "            }\n" +
                "            default:\n" +
                "                return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    private static int toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
