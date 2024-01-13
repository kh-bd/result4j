package dev.khbd.result4j.javac._try;

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
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName() {
                        return random().unwrap() ? Try.success("Alex") : Try.success("Sergei");
                    }
                    
                    public static Try<Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Try.success(rnd.nextBoolean());
                        }
                        return Try.failure(new RuntimeException("error"));
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
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName() {
                        return condition() ? Try.success(nameInternal().unwrap().toUpperCase()) : Try.failure(new RuntimeException("error"));
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Try<String> nameInternal() {
                        return Try.success("Alex");
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
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName() {
                        return condition() ?  Try.none() : Try.success(nameInternal().unwrap().toUpperCase());
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Try<String> nameInternal() {
                        return Try.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
