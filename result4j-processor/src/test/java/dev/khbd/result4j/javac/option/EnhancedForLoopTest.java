package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.List;

/**
 * @author Sergei_Khadanovich
 */
public class EnhancedForLoopTest extends AbstractPluginTest {

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
                "        for(var option : list) {\n" +
                "            var value = option.unwrap();\n" +
                "            result.add(value);\n" +
                "        }\n" +
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
                "        for(var option : list)\n" +
                "            result.add(option.unwrap());\n" +
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
    public void propagate_unwrapCallInForLoopSource_propagate() throws Exception {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Option;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Option<List<V>> sequence(Option<List<Option<V>>> mayBeList) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "        for(var option : mayBeList.unwrap()) {\n" +
                "            var value = option.unwrap();\n" +
                "            result.add(value);\n" +
                "        }\n" +
                "        return Option.some(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", Option.class);

        // call with None
        Option<List<String>> sequenced = (Option<List<String>>) method.invoke(null, Option.none());
        assertThat(sequenced.isEmpty()).isTrue();

        // call with empty list
        sequenced = (Option<List<String>>) method.invoke(null, Option.some(List.of()));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Option<List<String>>) method.invoke(null, Option.some(List.of(Option.some("1"), Option.none(), Option.some("3"))));
        assertThat(sequenced.isEmpty()).isTrue();

        // call with list not containing None
        sequenced = (Option<List<String>>) method.invoke(null, Option.some(List.of(Option.some("1"), Option.some("2"), Option.some("3"))));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }
}
