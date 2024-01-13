package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;

/**
 * @author Sergei_Khadanovich
 */
public class AssertTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInExpression() {
        String source = """
                package cases.assert_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        assert getName(flag).unwrap().equals("Alex");
                        return Either.left("error");
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.right("Alex");
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/assert_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));

    }

    @Test
    public void propagate_unwrapCallInDetails() {
        String source = """
                package cases.assert_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        assert 1 != 2 : getName(flag).unwrap();
                        return Either.left("error");
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.right("Alex");
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/assert_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));

    }
}
