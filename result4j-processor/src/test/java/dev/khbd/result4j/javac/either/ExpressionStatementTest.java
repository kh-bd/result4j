package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(int index) {
                        // result is ignored
                        name(index).unwrap();
                        return Either.right("Alex");
                    }
                                
                    private static Either<String, String> name(int index) {
                        if (index == 0) {
                            return Either.left("error");
                        }
                        return Either.right("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.expression_statement.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Either<String, String> greet = (Either<String, String>) method.invoke(null, 0);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");

        // invoke with not 0
        greet = (Either<String, String>) method.invoke(null, 10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");
    }

    @Test
    public void propagate_unwrapCallOnLabeledExpressionStatement_fail() {
        String source = """
                package cases.expression_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(int index) {
                        // result is ignored
                        label:
                        name(index).unwrap();
                        return Either.right("Alex");
                    }
                                
                    private static Either<String, String> name(int index) {
                        if (index == 0) {
                            return Either.left("error");
                        }
                        return Either.right("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
