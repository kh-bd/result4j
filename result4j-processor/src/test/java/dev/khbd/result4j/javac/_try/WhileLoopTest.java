package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
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
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                    
                    public static <V> Try<List<V>> sequence(List<Try<V>> list) {
                        Iterator<Try<V>> iterator = list.iterator();
                        
                        var result = new ArrayList<V>();
                        while(iterator.hasNext()) {
                            result.add(iterator.next().unwrap());
                        }
                        
                        return Try.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.while_loop.Main");
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
    public void propagate_oneStatementBlock() throws Exception {
        String source = """
                package cases.while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                    
                    public static <V> Try<List<V>> sequence(List<Try<V>> list) {
                        Iterator<Try<V>> iterator = list.iterator();
                        
                        var result = new ArrayList<V>();
                        while(iterator.hasNext())
                            result.add(iterator.next().unwrap());
                        
                        return Try.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.while_loop.Main");
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
    public void propagate_inWhileCondition_failCompilation() {
        String source = """
                package cases.while_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Try;
                                
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
                    
                    public static Try<Boolean> random(Random rnd) {
                        if (rnd.nextBoolean()) {
                            return Try.success(rnd.nextBoolean());
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
