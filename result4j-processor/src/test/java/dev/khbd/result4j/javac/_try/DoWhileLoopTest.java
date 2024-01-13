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
public class DoWhileLoopTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapInsideDoWhileLoopBlock() throws Exception {
        String source = """
                package cases.do_while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                    
                    public static <V> Try<List<V>> sequenceNotEmpty(List<Try<V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Try<V>> iterator = list.iterator();
                        do {
                            result.add(iterator.next().unwrap());
                        } while(iterator.hasNext());
                        
                        return Try.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Try<List<String>> sequenced = (Try<List<String>>) method.invoke(null, List.of(Try.success("1"),
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
                package cases.do_while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                    
                    public static <V> Try<List<V>> sequenceNotEmpty(List<Try<V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Try<V>> iterator = list.iterator();
                        do result.add(iterator.next().unwrap());
                        while(iterator.hasNext());
                        
                        return Try.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Try<List<String>> sequenced = (Try<List<String>>) method.invoke(null, List.of(Try.success("1"),
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
    public void propagate_unwrapInDoWhileCondition() {
        String source = """
                package cases.do_while_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Try;
                                
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
                    
                    public static Try<Boolean> random(Random rnd) {
                        if (rnd.nextBoolean()) {
                            return Try.success(rnd.nextBoolean());
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
