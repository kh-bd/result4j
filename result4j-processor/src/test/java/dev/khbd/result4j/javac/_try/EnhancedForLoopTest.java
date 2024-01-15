package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
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
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Try<List<V>> sequence(List<Try<V>> list) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "        for(var _try : list) {\n" +
                "            var value = _try.unwrap();\n" +
                "            result.add(value);\n" +
                "        }\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Try<List<String>> sequenced = (Try<List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isFailure()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Try<List<String>>) method.invoke(null, List.of(Try.success("1"),
                Try.failure(new RuntimeException("error")),
                Try.success("3")));
        assertThat(sequenced.isFailure()).isTrue();
        assertThat(sequenced.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with list not containing None
        sequenced = (Try<List<String>>) method.invoke(null, List.of(Try.success("1"), Try.success("2"), Try.success("3")));
        assertThat(sequenced.isFailure()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_oneStatementBody_propagate() throws Exception {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Try<List<V>> sequence(List<Try<V>> list) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "        for(var _try : list)\n" +
                "            result.add(_try.unwrap());\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Try<List<String>> sequenced = (Try<List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isFailure()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Try<List<String>>) method.invoke(null, List.of(Try.success("1"),
                Try.failure(new RuntimeException("error")),
                Try.success("3")));
        assertThat(sequenced.isFailure()).isTrue();
        assertThat(sequenced.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with list not containing None
        sequenced = (Try<List<String>>) method.invoke(null, List.of(Try.success("1"), Try.success("2"), Try.success("3")));
        assertThat(sequenced.isFailure()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapCallInForLoopSource_propagate() throws Exception {
        String source =
                "package cases.in_for_loop;\n" +
                "\n" +
                "import java.util.List;\n" +
                "import java.util.ArrayList;\n" +
                "import dev.khbd.result4j.core.Try;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static <V> Try<List<V>> sequence(Try<List<Try<V>>> mayBeList) {\n" +
                "        var result = new ArrayList<V>();\n" +
                "        for(var _try : mayBeList.unwrap()) {\n" +
                "            var value = _try.unwrap();\n" +
                "            result.add(value);\n" +
                "        }\n" +
                "        return Try.success(result);\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", Try.class);

        // call with None
        Try<List<String>> sequenced = (Try<List<String>>) method.invoke(null, Try.failure(new RuntimeException("error")));
        assertThat(sequenced.isFailure()).isTrue();
        assertThat(sequenced.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with empty list
        sequenced = (Try<List<String>>) method.invoke(null, Try.success(List.of()));
        assertThat(sequenced.isFailure()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Try<List<String>>) method.invoke(null, Try.success(List.of(Try.success("1"),
                Try.failure(new RuntimeException("error")),
                Try.success("3"))));
        assertThat(sequenced.isFailure()).isTrue();
        assertThat(sequenced.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // call with list not containing None
        sequenced = (Try<List<String>>) method.invoke(null, Try.success(List.of(Try.success("1"), Try.success("2"), Try.success("3"))));
        assertThat(sequenced.isFailure()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }
}
