package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class MethodCallTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtReceiverPosition_propagate() throws Exception {
        String source = """
                package cases.method_call;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(int index) {
                        var name = name(index).unwrap().toUpperCase();
                        return Option.some(name);
                    }
                                
                    private static Option<String> name(int index) {
                        if (index == 0) {
                            return Option.none();
                        }
                        return Option.some(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/method_call/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.method_call.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with negative
        greet = (Option<String>) method.invoke(null, -10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("ALEX");

        // invoke with positive
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("SERGEI");
    }

    @Test
    public void propagate_unwrapCallAtArgumentPosition_propagate() throws Exception {
        String source = """
                package cases.method_call;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(int index) {
                        var result = Option.some(name(index).unwrap());
                        return result;
                    }
                                
                    private static Option<String> name(int index) {
                        if (index == 0) {
                            return Option.none();
                        }
                        return Option.some(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/method_call/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.method_call.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with negative
        greet = (Option<String>) method.invoke(null, -10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with positive
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Sergei");
    }
}
