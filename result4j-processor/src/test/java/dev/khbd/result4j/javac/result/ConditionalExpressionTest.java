package dev.khbd.result4j.javac.result;

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
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName() {
                        return random().unwrap() ? Result.success("Alex") : Result.success("Sergei");
                    }
                    
                    public static Result<String, Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Result.success(rnd.nextBoolean());
                        }
                        return Result.error("error");
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
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName() {
                        return condition() ? Result.success(nameInternal().unwrap().toUpperCase()) : Result.error("error");
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Result<String, String> nameInternal() {
                        return Result.success("Alex");
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
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName() {
                        return condition() ?  Option.none() : Result.success(nameInternal().unwrap().toUpperCase());
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Result<String, String> nameInternal() {
                        return Result.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
