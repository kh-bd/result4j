package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Sergei_Khadanovich
 */
public class ForLoopTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInForLoopBody_propagate() throws Exception {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Option<List<V>> sequence(List<Option<V>> list) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "\n" +
                "        for(var iterator = list.iterator(); iterator.hasNext(); ) {\n" +
                "            result.add(iterator.next().unwrap());\n" +
                "        }\n" +
                "\n" +
                "        return Option.some(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Option<List<String>> sequenced = (Option<List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.none(), Option.some("3")));
        assertThat(sequenced.isEmpty()).isTrue();

        // call with list not containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.some("2"), Option.some("3")));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_oneStatementBody_propagate() throws Exception {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Option<List<V>> sequence(List<Option<V>> list) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "\n" +
                "        for(var iterator = list.iterator(); iterator.hasNext(); )\n" +
                "            result.add(iterator.next().unwrap());\n" +
                "\n" +
                "        return Option.some(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Option<List<String>> sequenced = (Option<List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.none(), Option.some("3")));
        assertThat(sequenced.isEmpty()).isTrue();

        // call with list not containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.some("2"), Option.some("3")));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapCallInForLoopInitializer_failCompilation() {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static void print() {\n" +
                "        for (int i = random().unwrap(); i < 10; i++) {\n" +
                "            System.out.println(\"Hello there!!!\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Option<Integer> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Option.some(rnd.nextInt());\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInForLoopCondition_failCompilation() {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static void print() {\n" +
                "        for (int i = 0; i < random().unwrap(); i++) {\n" +
                "            System.out.println(\"Hello there!!!\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Option<Integer> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Option.some(rnd.nextInt());\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInForLoopUpdateStatements_failCompilation() {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static void print() {\n" +
                "        for (int i = 0; i < 10; i += random().unwrap()) {\n" +
                "            System.out.println(\"Hello there!!!\");\n" +
                "        }\n" +
                "    }\n" +
                "\n" +
                "    public static Option<Integer> random() {\n" +
                "        var rnd = new Random();\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Option.some(rnd.nextInt());\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
