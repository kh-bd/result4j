package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ExpressionStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallOnExpressionStatement_propagate() throws Exception {
        String source = """
                package cases.expression_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(int index) {
                        // result is ignored
                        name(index).unwrap();
                        return Try.success("Alex");
                    }
                                
                    private static Try<String> name(int index) {
                        if (index == 0) {
                            return Try.failure(new RuntimeException("error"));
                        }
                        return Try.success("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.expression_statement.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Try<String> greet = (Try<String>) method.invoke(null, 0);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // invoke with not 0
        greet = (Try<String>) method.invoke(null, 10);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_unwrapCallOnLabeledExpressionStatement_fail() {
        String source = """
                package cases.expression_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(int index) {
                        // result is ignored
                        label:
                        name(index).unwrap();
                        return Try.success("Alex");
                    }
                                
                    private static Try<String> name(int index) {
                        if (index == 0) {
                            return Try.failure(new RuntimeException("error"));
                        }
                        return Try.success("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
