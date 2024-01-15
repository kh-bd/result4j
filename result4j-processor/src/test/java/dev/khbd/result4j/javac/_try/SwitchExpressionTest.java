package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class SwitchExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source =
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag).unwrap()) {\n" +
                "            case 1 -> Try.success(\"Alex\");\n" +
                "            default -> {\n" +
                "                yield Try.success(\"Alex\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Try.success(1) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_expression.Main");
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
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        label:\n" +
                "        return switch(toInteger(flag).unwrap()) {\n" +
                "            case 1 -> Try.success(\"Alex\");\n" +
                "            default -> {\n" +
                "                yield Try.success(\"Alex\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Try<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Try.success(1) : Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_ruleWithBlock_fail() {
        String source =
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag)) {\n" +
                "            default -> {\n" +
                "                var name = getName().unwrap();\n" +
                "                yield Try.success(name.toUpperCase());\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "\n" +
                "    private static Integer toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_ruleWithSingleExpression_fail() {
        String source =
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag)) {\n" +
                "            default -> Try.success(getName().unwrap().toUpperCase());\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Try<String> getName() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "\n" +
                "    private static Integer toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_statementCasesWithUnwrap_fail() {
        String source =
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag)) {\n" +
                "            default : {\n" +
                "                var name = Try.success(\"Alex\").unwrap();\n" +
                "                yield Try.success(name);\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Integer toInteger(boolean flag) {\n" +
                "        return flag ? 1 : 0;\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
