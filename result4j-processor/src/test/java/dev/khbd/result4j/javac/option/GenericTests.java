package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option get(Option value) {
                        Object obj = value.unwrap();
                        return Option.some(obj.toString());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Option.class);

        // call with none
        Option unwrapped = (Option) method.invoke(null, Option.none());
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option) method.invoke(null, Option.some(10));
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("10");
    }

    @Test
    public void propagate_useTypeVariable() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static <T> Option<T> get(Option<T> value) {
                        T obj = value.unwrap();
                        return Option.some(obj);
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Option.class);

        // call with none
        Option<String> unwrapped = (Option<String>) method.invoke(null, Option.none());
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, Option.some("Alex"));
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_useWildcardWithExtendBounds() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static <T> Option<T> get(Option<? extends T> value) {
                        return Option.some(value.unwrap());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Option.class);

        // call with none
        Option<String> unwrapped = (Option<String>) method.invoke(null, Option.none());
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, Option.some("Alex"));
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_useWildcardWithNoBounds() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Object> get(Option<?> value) {
                        return Option.some(value.unwrap());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Option.class);

        // call with none
        Option<String> unwrapped = (Option<String>) method.invoke(null, Option.none());
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, Option.some("Alex"));
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_useWildcardWithSuperBounds() throws Exception {
        String source = """
                package cases.generics;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static <T> Option<Object> get(Option<? super T> value) {
                        return Option.some(value.unwrap());
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/generics/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.generics.Main");
        Method method = clazz.getMethod("get", Option.class);

        // call with none
        Option<String> unwrapped = (Option<String>) method.invoke(null, Option.none());
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, Option.some("Alex"));
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");
    }
}
