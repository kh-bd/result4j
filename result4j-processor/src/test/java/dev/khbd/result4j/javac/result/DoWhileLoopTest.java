package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
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
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                    
                    public static <V> Result<String, List<V>> sequenceNotEmpty(List<Result<String, V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Result<String, V>> iterator = list.iterator();
                        do {
                            result.add(iterator.next().unwrap());
                        } while(iterator.hasNext());
                        
                        return Result.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Result<String, List<String>> sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.error("error"), Result.success("3")));
        assertThat(sequenced.isError()).isTrue();
        assertThat(sequenced.getError()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.success("2"), Result.success("3")));
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_oneStatementBlock() throws Exception {
        String source = """
                package cases.do_while_loop;
                                
                import java.util.Iterator;
                import java.util.List;
                import java.util.ArrayList;
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                    
                    public static <V> Result<String, List<V>> sequenceNotEmpty(List<Result<String, V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Result<String, V>> iterator = list.iterator();
                        do result.add(iterator.next().unwrap());
                        while(iterator.hasNext());
                        
                        return Result.success(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Result<String, List<String>> sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.error("error"), Result.success("3")));
        assertThat(sequenced.isError()).isTrue();
        assertThat(sequenced.getError()).isEqualTo("error");

        // call with list not containing None
        sequenced = (Result<String, List<String>>) method.invoke(null, List.of(Result.success("1"), Result.success("2"), Result.success("3")));
        assertThat(sequenced.isSuccess()).isTrue();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void propagate_unwrapInDoWhileCondition() {
        String source = """
                package cases.do_while_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Result;
                                
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
                    
                    public static Result<String, Boolean> random(Random rnd) {
                        if (rnd.nextBoolean()) {
                            return Result.success(rnd.nextBoolean());
                        }
                        return Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
