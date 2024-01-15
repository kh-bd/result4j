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
public class DoWhileLoopTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapInsideDoWhileLoopBlock() throws Exception {
        String source =
                "package cases.do_while_loop;\n" +
                "\n" +
                "import java.util.Iterator;\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Option<List<V>> sequenceNotEmpty(List<Option<V>> list) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "\n" +
                "        Iterator<Option<V>> iterator = list.iterator();\n" +
                "        do {\n" +
                "            result.add(iterator.next().unwrap());\n" +
                "        } while(iterator.hasNext());\n" +
                "\n" +
                "        return Option.some(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Option<List<String>> sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.none(), Option.some("3")));
        assertThat(sequenced.isEmpty()).isTrue();

        // call with list not containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.some("2"), Option.some("3")));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_oneStatementBlock() throws Exception {
        String source =
                "package cases.do_while_loop;\n" +
                "\n" +
                "import java.util.Iterator;\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Option<List<V>> sequenceNotEmpty(List<Option<V>> list) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "\n" +
                "        Iterator<Option<V>> iterator = list.iterator();\n" +
                "        do result.add(iterator.next().unwrap());\n" +
                "        while(iterator.hasNext());\n" +
                "\n" +
                "        return Option.some(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.getClassLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Option<List<String>> sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.none(), Option.some("3")));
        assertThat(sequenced.isEmpty()).isTrue();

        // call with list not containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.some("2"), Option.some("3")));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapInDoWhileCondition() {
        String source =
                "package cases.do_while_loop;\n" +
                "\n" +
                "import java.util.Random;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static int count() {\n" +
                "        var rnd = new Random();\n" +
                "\n" +
                "        int count = 0;\n" +
                "\n" +
                "        do {\n" +
                "            count++;\n" +
                "            // fail to compile\n" +
                "        } while(random(rnd).unwrap());\n" +
                "\n" +
                "        return count;\n" +
                "    }\n" +
                "\n" +
                "    public static Option<Boolean> random(Random rnd) {\n" +
                "        if (rnd.nextBoolean()) {\n" +
                "            return Option.some(rnd.nextBoolean());\n" +
                "        }\n" +
                "        return Option.none();\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
