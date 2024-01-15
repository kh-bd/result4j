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
public class IfTest extends AbstractPluginTest {

    @Test
    public void propagate_inCondition_failCompilation() {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        if (random().unwrap().booleanValue()) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        } else {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Boolean> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Try.success(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }


    @Test
    public void propagate_inElseIfCondition_failCompilation() {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        if (false) {\n" +
                "            return Try.success(\"Alex\");\n" +
                "        } else if (random().unwrap().booleanValue()) {\n" +
                "            return Try.success(\"Sergei\");\n" +
                "        } else {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Try<Boolean> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Try.success(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inThenBlock_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.success(getName().unwrap().toUpperCase());\n" +
                "        } else {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Try<String> name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Try.failure(new RuntimeException(\"error\"));\n" +
                "        } else {\n" +
                "            return Try.success(getName().unwrap().toUpperCase());\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) {\n" +
                "        if (flag) return Try.success(getName().unwrap().toUpperCase());\n" +
                "        else return Try.failure(new RuntimeException(\"error\"));\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Try<String> name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Try<String> getName(boolean flag) {\n" +
                "        if (flag) return Try.failure(new RuntimeException(\"error\"));\n" +
                "        else return Try.success(getName().unwrap().toUpperCase());\n" +
                "    }\n" +
                "\n" +
                "    public static Try<String> getName() {\n" +
                "        return Try.success(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
