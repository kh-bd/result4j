package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static <V> Either<String, List<V>> sequence(List<Either<String, V>> list) {
                        var result = new ArrayList<V>();
                        for(var option : list) {
                            var value = option.unwrap();
                            result.add(value);
                        }
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Either<String, List<String>> sequenced = (Either<String, List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.left("error"), Either.right("3")));
        assertThat(sequenced.isLeft()).isTrue();
        assertThat(sequenced.getLeft()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.right("2"), Either.right("3")));
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_oneStatementBody_propagate() throws Exception {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static <V> Either<String, List<V>> sequence(List<Either<String, V>> list) {
                        var result = new ArrayList<V>();
                        for(var option : list)
                            result.add(option.unwrap());
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", List.class);

        // call with empty list
        Either<String, List<String>> sequenced = (Either<String, List<String>>) method.invoke(null, List.of());
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.left("error"), Either.right("3")));
        assertThat(sequenced.isLeft()).isTrue();
        assertThat(sequenced.getLeft()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.right("2"), Either.right("3")));
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapCallInForLoopSource_propagate() throws Exception {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static <V> Either<String, List<V>> sequence(Either<String, List<Either<String, V>>> mayBeList) {
                        var result = new ArrayList<V>();
                        for(var option : mayBeList.unwrap()) {
                            var value = option.unwrap();
                            result.add(value);
                        }
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_for_loop.Main");
        Method method = clazz.getMethod("sequence", Either.class);

        // call with None
        Either<String, List<String>> sequenced = (Either<String, List<String>>) method.invoke(null, Either.left("error"));
        assertThat(sequenced.isLeft()).isTrue();
        assertThat(sequenced.getLeft()).isEqualTo("error");

        // call with empty list
        sequenced = (Either<String, List<String>>) method.invoke(null, Either.right(List.of()));
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of());

        // call with list containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, Either.right(List.of(Either.right("1"), Either.left("error"), Either.right("3"))));
        assertThat(sequenced.isLeft()).isTrue();
        assertThat(sequenced.getLeft()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, Either.right(List.of(Either.right("1"), Either.right("2"), Either.right("3"))));
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of("1", "2", "3"));
    }
}
