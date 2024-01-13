package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;

/**
 * @author Sergei_Khadanovich
 */
public class ConditionalExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInCondition_failCompilation() {
        String source = """
                package cases.conditional_expression;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName() {
                        return random().unwrap() ? Either.right("Alex") : Either.right("Sergei");
                    }
                    
                    public static Either<String, Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextBoolean());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInThen_failCompilation() {
        String source = """
                package cases.conditional_expression;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName() {
                        return condition() ? Either.right(nameInternal().unwrap().toUpperCase()) : Either.left("error");
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Either<String, String> nameInternal() {
                        return Either.right("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInElse_failCompilation() {
        String source = """
                package cases.conditional_expression;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName() {
                        return condition() ?  Option.none() : Either.right(nameInternal().unwrap().toUpperCase());
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Either<String, String> nameInternal() {
                        return Either.right("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
