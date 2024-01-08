package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Option;
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        throw createThrow(flag).unwrap();
                    }
                                
                    private static Option<RuntimeException> createThrow(boolean flag) {
                        if (flag) {
                            return Option.some(new RuntimeException());
                        }
                        return Option.none();
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
        Option<?> greet = (Option<?>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInLabeledThrowExpression() {
        String source = """
                package cases.throw_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        label:
                        throw createThrow(flag).unwrap();
                    }
                                
                    private static Option<RuntimeException> createThrow(boolean flag) {
                        if (flag) {
                            return Option.some(new RuntimeException());
                        }
                        return Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/throw_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
