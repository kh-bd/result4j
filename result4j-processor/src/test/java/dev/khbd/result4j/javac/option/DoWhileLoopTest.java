package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

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
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                    
                    public static <V> Option<List<V>> sequenceNotEmpty(List<Option<V>> list) {
                        var result = new ArrayList<V>();
                        
                        Iterator<Option<V>> iterator = list.iterator();
                        do {
                            result.add(iterator.next().unwrap());
                        } while(iterator.hasNext());
                        
                        return Option.some(result);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/do_while_loop/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.do_while_loop.Main");
        Method method = clazz.getMethod("sequenceNotEmpty", List.class);

        // call with list containing None
        Option<List<String>> sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.none(), Option.some("3")));
        assertThat(sequenced.isEmpty()).isTrue();

        // call with list not containing None
        sequenced = (Option<List<String>>) method.invoke(null, List.of(Option.some("1"), Option.some("2"), Option.some("3")));
        assertThat(sequenced.isEmpty()).isFalse();
        assertThat(sequenced.get()).isEqualTo(List.of("1", "2", "3"));
    }
}
