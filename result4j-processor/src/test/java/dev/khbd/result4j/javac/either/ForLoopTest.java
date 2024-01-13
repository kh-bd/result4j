package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
        String source = """
                package cases.in_for_loop;
                                
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static <V> Either<String, List<V>> sequence(List<Either<String, V>> list) {
                        var result = new ArrayList<V>();
                        
                        for(var iterator = list.iterator(); iterator.hasNext(); ) {
                            result.add(iterator.next().unwrap());
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
                        
                        for(var iterator = list.iterator(); iterator.hasNext(); )
                            result.add(iterator.next().unwrap());
                        
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
    public void propagate_unwrapCallInForLoopInitializer_failCompilation() {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static void print() {
                        for (int i = random().unwrap(); i < 10; i++) {
                            System.out.println("Hello there!!!");
                        }
                    }
                    
                    public static Either<String, Integer> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextInt());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInForLoopCondition_failCompilation() {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static void print() {
                        for (int i = 0; i < random().unwrap(); i++) {
                            System.out.println("Hello there!!!");
                        }
                    }
                    
                    public static Either<String, Integer> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextInt());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInForLoopUpdateStatements_failCompilation() {
        String source = """
                package cases.in_for_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static void print() {
                        for (int i = 0; i < 10; i += random().unwrap()) {
                            System.out.println("Hello there!!!");
                        }
                    }
                    
                    public static Either<String, Integer> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextInt());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_for_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
