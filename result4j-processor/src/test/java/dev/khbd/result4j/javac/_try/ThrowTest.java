package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ThrowTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInThrowExpression() throws Exception {
        String source = """
                package cases.throw_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        throw createThrow(flag).unwrap();
                    }
                                
                    private static Try<RuntimeException> createThrow(boolean flag) {
                        if (flag) {
                            return Try.success(new RuntimeException());
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.throw_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with true
        Throwable error = catchThrowable(() -> method.invoke(null, true));
        assertThat(error).isInstanceOf(InvocationTargetException.class)
                .hasCauseInstanceOf(RuntimeException.class);

//      invoke with false
        Try<?> greet = (Try<?>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledThrowExpression() {
        String source = """
                package cases.throw_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        label:
                        throw createThrow(flag).unwrap();
                    }
                                
                    private static Try<RuntimeException> createThrow(boolean flag) {
                        if (flag) {
                            return Try.success(new RuntimeException());
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
