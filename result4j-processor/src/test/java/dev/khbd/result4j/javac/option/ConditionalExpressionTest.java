package dev.khbd.result4j.javac.option;

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
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName() {
                        return random().unwrap() ? Option.some("Alex") : Option.some("Sergei");
                    }
                    
                    public static Option<Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Option.some(rnd.nextBoolean());
                        }
                        return Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        System.out.println(result);
        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInThen_failCompilation() {
        String source = """
                package cases.conditional_expression;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName() {
                        return condition() ? Option.some(nameInternal().unwrap().toUpperCase()) : Option.none();
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Option<String> nameInternal() {
                        return Option.some("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        System.out.println(result);
        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInElse_failCompilation() {
        String source = """
                package cases.conditional_expression;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName() {
                        return condition() ?  Option.none() : Option.some(nameInternal().unwrap().toUpperCase());
                    }
                    
                    public static boolean condition() {
                        var rnd = new Random();
                        return rnd.nextBoolean();
                    }
                    
                    public static Option<String> nameInternal() {
                        return Option.some("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        System.out.println(result);
        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
