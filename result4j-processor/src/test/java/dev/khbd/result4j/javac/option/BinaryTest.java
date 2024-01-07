package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class BinaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inBinaryBoth() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag1, boolean flag2) {
                        var result = getInt(flag1).unwrap() + getInt(flag2).unwrap();
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class, boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false, false);
        assertThat(sum.isEmpty()).isTrue();

        sum = (Option<Integer>) method.invoke(null, true, false);
        assertThat(sum.isEmpty()).isTrue();

        sum = (Option<Integer>) method.invoke(null, false, true);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(20);
    }

    @Test
    public void propagate_inBinaryLeft() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag) {
                        var result = getInt(flag).unwrap() + 1;
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inBinaryRight() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag) {
                        var result = getInt(flag).unwrap();
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inComplexBinary() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag) {
                        var result = (getInt(flag).unwrap() + getInt(flag).unwrap()) + getInt(flag).unwrap();
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(30);
    }
}
