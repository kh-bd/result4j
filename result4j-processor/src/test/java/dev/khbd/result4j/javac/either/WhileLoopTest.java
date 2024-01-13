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
public class WhileLoopTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapInsideWhileLoopBlock() throws Exception {
        String source = """
                package cases.while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                    
                    public static <V> Either<String, List<V>> sequence(List<Either<String, V>> list) {
                        Iterator<Either<String, V>> iterator = list.iterator();
                        
                        var result = new ArrayList<V>();
                        while(iterator.hasNext()) {
                            result.add(iterator.next().unwrap());
                        }
                        
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.while_loop.Main");
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
    public void propagate_oneStatementBlock() throws Exception {
        String source = """
                package cases.while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                    
                    public static <V> Either<String, List<V>> sequence(List<Either<String, V>> list) {
                        Iterator<Either<String, V>> iterator = list.iterator();
                        
                        var result = new ArrayList<V>();
                        while(iterator.hasNext())
                            result.add(iterator.next().unwrap());
                        
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.while_loop.Main");
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
    public void propagate_inWhileCondition_failCompilation() {
        String source = """
                package cases.while_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static int count() {
                        var rnd = new Random();
                        
                        int count = 0;
                        // fail to compile
                        while (random(rnd).unwrap()) {
                            count++;
                        }
                        
                        return count;
                    }
                    
                    public static Either<String, Boolean> random(Random rnd) {
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextBoolean());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
