package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei Khadanovich
 */
public class GenericTests extends AbstractPluginTest {
    
    @Test
    public void propagate_useRawType() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result get(Result value) {
                        Object obj = value.unwrap();
                        return Result.success(obj.toString());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Result.class);

        // call with none
        Result unwrapped = (Result) method.invoke(null, Result.error("error"));
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result) method.invoke(null, Result.success(10));
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("10");
    }

    @Test
    public void propagate_useTypeVariable() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static <E, R> Result<E, R> get(Result<E, R> value) {
                        R obj = value.unwrap();
                        return Result.success(obj);
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Result.class);

        // call with none
        Result<String, Integer> unwrapped = (Result<String, Integer>) method.invoke(null, Result.error("error"));
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<String, Integer>) method.invoke(null, Result.success(10));
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo(10);
    }

    @Test
    public void propagate_useWildcardWithExtendBounds() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static <E, R> Result<E, R> get(Result<? extends E, ? extends R> value) {
                        return Result.success(value.unwrap());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Result.class);

        // call with none
        Result<String, String> unwrapped = (Result<String, String>) method.invoke(null, Result.error("error"));
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<String, String>) method.invoke(null, Result.success("Alex"));
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_useWildcardWithNoBounds() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<?, ?> get(Result<?, ?> value) {
                        return Result.success(value.unwrap());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Result.class);

        // call with none
        Result<?, ?> unwrapped = (Result<?, ?>) method.invoke(null, Result.error("error"));
        assertThat(unwrapped.isError()).isTrue();

        unwrapped = (Result<?, ?>) method.invoke(null, Result.success("Alex"));
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_useWildcardWithSuperBounds() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static <E, R> Result<?, ?> get(Result<? super E, ? super R> value) {
                        return Result.success(value.unwrap());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Result.class);

        // call with none
        Result<?, ?> unwrapped = (Result<?, ?>) method.invoke(null, Result.error("error"));
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<?, ?>) method.invoke(null, Result.success("Alex"));
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }
}
