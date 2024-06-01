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
public class ExpressionStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallOnExpressionStatement_propagate() throws Exception {
        String source = """
                package cases.expression_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> greet(int index) {
                        // result is ignored
                        name(index).unwrap();
                        return Result.success("Alex");
                    }
                                
                    private static Result<String, String> name(int index) {
                        if (index == 0) {
                            return Result.error("error");
                        }
                        return Result.success("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.expression_statement.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Result<String, String> greet = (Result<String, String>) method.invoke(null, 0);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");

        // invoke with not 0
        greet = (Result<String, String>) method.invoke(null, 10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");
    }

    @Test
    public void propagate_unwrapCallOnLabeledExpressionStatement_fail() {
        String source = """
                package cases.expression_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> greet(int index) {
                        // result is ignored
                        label:
                        name(index).unwrap();
                        return Result.success("Alex");
                    }
                                
                    private static Result<String, String> name(int index) {
                        if (index == 0) {
                            return Result.error("error");
                        }
                        return Result.success("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
