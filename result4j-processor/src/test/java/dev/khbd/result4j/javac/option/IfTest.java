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
public class IfTest extends AbstractPluginTest {

    @Test
    public void propagate_inCondition() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(int flag) {\n" +
                        "        if (getFlag(flag).unwrap().booleanValue()) {\n" +
                        "            return Option.some(\"Alex\");\n" +
                        "        }\n" +
                        "        return Option.some(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> getFlag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", int.class);

        // call with 0
        Option<String> name = (Option<String>) method.invoke(null, 0);
        assertThat(name.isEmpty()).isTrue();

        // call with positive number
        name = (Option<String>) method.invoke(null, 10);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");

        // call with negative number
        name = (Option<String>) method.invoke(null, -10);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_inComplexConditionButAtLeftSide() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(int flag) {\n" +
                        "        if (getFlag(flag).unwrap().booleanValue() && flag > 0) {\n" +
                        "            return Option.some(\"Alex\");\n" +
                        "        }\n" +
                        "        return Option.some(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> getFlag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", int.class);

        // call with 0
        Option<String> name = (Option<String>) method.invoke(null, 0);
        assertThat(name.isEmpty()).isTrue();

        // call with positive number
        name = (Option<String>) method.invoke(null, 10);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");

        // call with negative number
        name = (Option<String>) method.invoke(null, -10);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_inElseIfCondition_failCompilation() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        } else if (getFlag(flag).unwrap().booleanValue()) {\n" +
                        "            return Option.some(\"Alex\");\n" +
                        "        }\n" +
                        "        return Option.some(\"Sergei\");\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> getFlag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", int.class);

        // call with 0
        Option<String> name = (Option<String>) method.invoke(null, 0);
        assertThat(name.isEmpty()).isTrue();

        // call with positive number
        name = (Option<String>) method.invoke(null, 10);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");

        // call with negative number
        name = (Option<String>) method.invoke(null, -10);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_inRightSideOfComplexCondition_failCompilation() {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(int flag) {\n" +
                        "        if (flag > 0 && getFlag(flag).unwrap().booleanValue()) {\n" +
                        "            return Option.some(\"Alex\");\n" +
                        "        }\n" +
                        "        return Option.none();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<Boolean> getFlag(int flag) {\n" +
                        "        if (flag == 0) {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "        return Option.some(flag > 0);\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inThenBlock_success() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Option.some(getName().unwrap().toUpperCase());\n" +
                        "        } else {\n" +
                        "            return Option.none();\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Option<String> name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(boolean flag) {\n" +
                        "        if (flag) {\n" +
                        "            return Option.none();\n" +
                        "        } else {\n" +
                        "            return Option.some(getName().unwrap().toUpperCase());\n" +
                        "        }\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isTrue();
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(boolean flag) {\n" +
                        "        if (flag) return Option.some(getName().unwrap().toUpperCase());\n" +
                        "        else return Option.none();\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Option<String> name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source = "package cases.if_statement;\n" +
                        "\n" +
                        "import dev.khbd.result4j.core.Option;\n" +
                        "\n" +
                        "public class Main {\n" +
                        "\n" +
                        "    public static Option<String> getName(boolean flag) {\n" +
                        "        if (flag) return Option.none();\n" +
                        "        else return Option.some(getName().unwrap().toUpperCase());\n" +
                        "    }\n" +
                        "\n" +
                        "    public static Option<String> getName() {\n" +
                        "        return Option.some(\"Alex\");\n" +
                        "    }\n" +
                        "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isTrue();
    }
}
