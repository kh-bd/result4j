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
public class SynchronizedBlockTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInBlock() throws Exception {
        String source =
                "package cases.sync_block;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(boolean flag) {\n" +
                "        synchronized (Main.class) {\n" +
                "            var name = getName(flag).unwrap();\n" +
                "            return Option.some(name.toUpperCase());\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(boolean flag) {\n" +
                "        return flag ? Option.some(\"Alex\") : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.sync_block.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with false
        Option<String> greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();

//      invoke with true
        greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInSyncExpression() throws Exception {
        String source =
                "package cases.sync_block;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(boolean flag) {\n" +
                "        synchronized (getName(flag).unwrap()) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(boolean flag) {\n" +
                "        return flag ? Option.some(\"Alex\") : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.sync_block.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with false
        Option<String> greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInLabeledSyncExpression() {
        String source =
                "package cases.sync_block;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Option<String> greet(boolean flag) {\n" +
                "        label:\n" +
                "        synchronized (getName(flag).unwrap()) {\n" +
                "            return Option.none();\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    private static Option<String> getName(boolean flag) {\n" +
                "        return flag ? Option.some(\"Alex\") : Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
