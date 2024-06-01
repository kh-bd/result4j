package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
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
        String source = """
                package cases.in_for_loop;
                                
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                
                    public static <V> Result<String, List<V>> sequence(List<Result<String, V>> list) {
                        var result = new ArrayList<V>();
                        for(var option : list) {
                            var value = option.unwrap();
                            result.add(value);
                        }
                        return Result.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Result<String, List<String>> sequenced = (Result<String, List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.error("error"), Result.success("3")));
        assertThat(sequenced.isError()).isTrue();
        assertThat(sequenced.getError()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.success("2"), Result.success("3")));
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_oneStatementBody_propagate() throws Exception {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                
                    public static <V> Result<String, List<V>> sequence(List<Result<String, V>> list) {
                        var result = new ArrayList<V>();
                        for(var option : list)
                            result.add(option.unwrap());
                        return Result.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Result<String, List<String>> sequenced = (Result<String, List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.error("error"), Result.success("3")));
        assertThat(sequenced.isError()).isTrue();
        assertThat(sequenced.getError()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.success("2"), Result.success("3")));
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapCallInForLoopSource_propagate() throws Exception {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                
                    public static <V> Result<String, List<V>> sequence(Result<String, List<Result<String, V>>> mayBeList) {
                        var result = new ArrayList<V>();
                        for(var option : mayBeList.unwrap()) {
                            var value = option.unwrap();
                            result.add(value);
                        }
                        return Result.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", Result.class);

        // call with None
        Result<String, List<String>> sequenced = (Result<String, List<String>>) method.invoke(null, Result.error("error"));
        assertThat(sequenced.isError()).isTrue();
        assertThat(sequenced.getError()).isEqualTo("error");

        // call with empty list
        sequenced = (Result<String, List<String>>) method.invoke(null, Result.success(List.of()));
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, Result.success(List.of(Result.success("1"), Result.error("error"), Result.success("3"))));
        assertThat(sequenced.isError()).isTrue();
        assertThat(sequenced.getError()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, Result.success(List.of(Result.success("1"), Result.success("2"), Result.success("3"))));
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }
}
