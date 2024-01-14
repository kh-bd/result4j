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
public class SwitchExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source =
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag).unwrap()) {\n" +
                "            case 1 -> Option.some(\"Alex\");\n" +
                "            default -> {\n" +
                "                yield Option.some(\"Alex\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Option.some(1) : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_expression.Main");
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
                "package cases.switch_expression;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        label:\n" +
                "        return switch(toInteger(flag).unwrap()) {\n" +
                "            case 1 -> Option.some(\"Alex\");\n" +
                "            default -> {\n" +
                "                yield Option.some(\"Alex\");\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Option<Integer> toInteger(boolean flag) {\n" +
                "        return flag ? Option.some(1) : Option.none();\n" +
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
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag)) {\n" +
                "            default -> {\n" +
                "                var name = getName().unwrap();\n" +
                "                yield Option.some(name.toUpperCase());\n" +
                "            }\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName() {\n" +
                "        return Option.some(\"Alex\");\n" +
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
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag)) {\n" +
                "            default -> Option.some(getName().unwrap().toUpperCase());\n" +
                "        };\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName() {\n" +
                "        return Option.some(\"Alex\");\n" +
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
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<?> greet(boolean flag) {\n" +
                "        return switch(toInteger(flag)) {\n" +
                "            default : {\n" +
                "                var name = Option.some(\"Alex\").unwrap();\n" +
                "                yield Option.some(name);\n" +
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
