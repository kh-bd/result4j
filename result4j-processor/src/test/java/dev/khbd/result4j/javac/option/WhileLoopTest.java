package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                    
                    public static <V> Option<List<V>> sequence(List<Option<V>> list) {
                        Iterator<Option<V>> iterator = list.iterator();
                        
                        var result = new ArrayList<V>();
                        while(iterator.hasNext()) {
                            result.add(iterator.next().unwrap());
                        }
                        
                        return Option.some(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.while_loop.Main");
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
    public void propagate_inWhileCondition_failCompilation() {
        String source = """
                package cases.while_loop;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Option;
                                
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
                    
                    public static Option<Boolean> random(Random rnd) {
                        if (rnd.nextBoolean()) {
                            return Option.some(rnd.nextBoolean());
                        }
                        return Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/while_loop/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
