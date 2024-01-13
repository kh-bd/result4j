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
public class DoWhileLoopTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapInsideDoWhileLoopBlock() throws Exception {
        String source = """
                package cases.do_while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                    
                    public static <V> Either<String, List<V>> sequenceNotEmpty(List<Either<String, V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Either<String, V>> iterator = list.iterator();
                        do {
                            result.add(iterator.next().unwrap());
                        } while(iterator.hasNext());
                        
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Either<String, List<String>> sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.left("error"), Either.right("3")));
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
                package cases.do_while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                    
                    public static <V> Either<String, List<V>> sequenceNotEmpty(List<Either<String, V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Either<String, V>> iterator = list.iterator();
                        do result.add(iterator.next().unwrap());
                        while(iterator.hasNext());
                        
                        return Either.right(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Either<String, List<String>> sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.left("error"), Either.right("3")));
        assertThat(sequenced.isLeft()).isTrue();
        assertThat(sequenced.getLeft()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Either<String, List<String>>) method.invoke(null, List.of(Either.right("1"), Either.right("2"), Either.right("3")));
        assertThat(sequenced.isRight()).isTrue();
        assertThat(sequenced.getRight()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapInDoWhileCondition() {
        String source = """
                package cases.do_while_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static int count() {
                        var rnd = new Random();
                        
                        int count = 0;
                        
                        do {
                            count++;
                            // fail to compile
                        } while(random(rnd).unwrap());
                        
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

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
