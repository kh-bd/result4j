package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class NewArrayTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtIndexPosition() throws Exception {
        String source = """
                package cases.new_array;
                                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Integer> getArraySize(boolean flag) {
                        var array = new String[getSize(flag).unwrap()][getSize(flag).unwrap()];
                        return Option.some(array[0].length);
                    }
                    
                    private static Option<Integer> getSize(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/new_array/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.new_array.Main");
        Method method = clazz.getMethod("getArraySize", boolean.class);

        // invoke with true
        Option<Integer> greet = (Option<Integer>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo(10);

        // invoke with false
        greet = (Option<Integer>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallAtExpressionPosition() throws Exception {
        String source = """
                package cases.new_array;
                                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Integer> getArraySize(boolean flag) {
                        var array = new Integer[] { getSize(flag).unwrap(), getSize(flag).unwrap() };
                        return Option.some(array.length);
                    }
                    
                    private static Option<Integer> getSize(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/new_array/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.new_array.Main");
        Method method = clazz.getMethod("getArraySize", boolean.class);

        // invoke with true
        Option<Integer> greet = (Option<Integer>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo(2);

        // invoke with false
        greet = (Option<Integer>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }
}
