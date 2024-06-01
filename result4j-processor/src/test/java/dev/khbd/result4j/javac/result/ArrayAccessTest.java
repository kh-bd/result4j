package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                
                    public static Result<String, String> greet(boolean flag) {
                        var name = getArray(flag).unwrap()[0];
                        return Result.success(name);
                    }
                    
                    private static Result<String, String[]> getArray(boolean flag) {
                        return flag ? Result.success(new String[] {"Alex"}) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallAtIndexPosition() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                
                    public static Result<String, String> greet(boolean flag) {
                        var names = new String[] {"Alex"};
                        var name = names[getIndex(flag).unwrap()];
                        return Result.success(name);
                    }
                    
                    private static Result<String, Integer> getIndex(boolean flag) {
                        return flag ? Result.success(0) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallAtInstanceAndIndexPositions() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                
                    public static Result<String, String> greet(boolean flag) {
                        var name = getArray(flag).unwrap()[getIndex(flag).unwrap()];
                        return Result.success(name);
                    }
                    
                    private static Result<String, String[]> getArray(boolean flag) {
                        return flag ? Result.success(new String[] {"Alex"}) : Result.error("error");
                    }
                    
                    private static Result<String, Integer> getIndex(boolean flag) {
                        return flag ? Result.success(0) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }
}
