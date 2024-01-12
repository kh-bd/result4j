package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ArrayAccessTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtInstancePosition() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                
                    public static Try<String> greet(boolean flag) {
                        var name = getArray(flag).unwrap()[0];
                        return Try.success(name);
                    }
                    
                    private static Try<String[]> getArray(boolean flag) {
                        return flag ? Try.success(new String[] {"Alex"}) : Try.failure(new RuntimeException());
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void propagate_unwrapCallAtIndexPosition() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                
                    public static Try<String> greet(boolean flag) {
                        var names = new String[] {"Alex"};
                        var name = names[getIndex(flag).unwrap()];
                        return Try.success(name);
                    }
                    
                    private static Try<Integer> getIndex(boolean flag) {
                        return flag ? Try.success(0) : Try.failure(new RuntimeException());
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void propagate_unwrapCallAtInstanceAndIndexPositions() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                
                    public static Try<String> greet(boolean flag) {
                        var name = getArray(flag).unwrap()[getIndex(flag).unwrap()];
                        return Try.success(name);
                    }
                    
                    private static Try<String[]> getArray(boolean flag) {
                        return flag ? Try.success(new String[] {"Alex"}) : Try.failure(new RuntimeException());
                    }
                    
                    private static Try<Integer> getIndex(boolean flag) {
                        return flag ? Try.success(0) : Try.failure(new RuntimeException());
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class);
    }
}
