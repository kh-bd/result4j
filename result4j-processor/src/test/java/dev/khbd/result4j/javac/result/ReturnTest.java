package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ReturnTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReturn_propagate() throws Exception {
        String source = """
                package cases.in_return;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<RuntimeException, String> greet(int index) {
                        return Result.success(name(index).unwrap());
                    }
                                
                    private static Result<RuntimeException, String> name(int index) {
                        if (index == 0) {
                            return Result.error(new RuntimeException("error"));
                        }
                        return Result.success(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Result<Exception, String> greet = (Result<Exception, String>) method.invoke(null, 0);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // invoke with negative
        greet = (Result<Exception, String>) method.invoke(null, -10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with positive
        greet = (Result<Exception, String>) method.invoke(null, 10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInLabeledReturn_propagate() {
        String source = """
                package cases.in_return;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<RuntimeException, String> greet(int index) {
                        label:
                        return Result.success(name(index).unwrap());
                    }
                                
                    private static Result<RuntimeException, String> name(int index) {
                        if (index == 0) {
                            return Result.error(new RuntimeException("error"));
                        }
                        return Result.success(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
