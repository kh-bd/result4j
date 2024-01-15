package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        if (random().unwrap().booleanValue()) {\n" +
                "            return Either.right(\"Alex\");\n" +
                "        } else {\n" +
                "            return Either.left(\"error\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Boolean> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Either.right(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
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
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        if (false) {\n" +
                "            return Either.right(\"Alex\");\n" +
                "        } else if (random().unwrap().booleanValue()) {\n" +
                "            return Either.right(\"Sergei\");\n" +
                "        } else {\n" +
                "            return Either.left(\"error\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, Boolean> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Either.right(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Either.left(\"error\");\n" +
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
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.right(getName().unwrap().toUpperCase());\n" +
                "        } else {\n" +
                "            return Either.left(\"error\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return Either.right(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Either<String, String> name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with false
        name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) {\n" +
                "            return Either.left(\"error\");\n" +
                "        } else {\n" +
                "            return Either.right(getName().unwrap().toUpperCase());\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return Either.right(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) return Either.right(getName().unwrap().toUpperCase());\n" +
                "        else return Either.left(\"error\");\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return Either.right(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Either<String, String> name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with false
        name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source =
                "package cases.if_statement;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<String, String> getName(boolean flag) {\n" +
                "        if (flag) return Either.left(\"error\");\n" +
                "        else return Either.right(getName().unwrap().toUpperCase());\n" +
                "    }\n" +
                "\n" +
                "    public static Either<String, String> getName() {\n" +
                "        return Either.right(\"Alex\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }
}
