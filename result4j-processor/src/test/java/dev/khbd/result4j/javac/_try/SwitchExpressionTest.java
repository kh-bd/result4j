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
public class SwitchExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Try.success("Alex");
                            default -> {
                                yield Try.success("Alex");
                            }
                        };
                    }
                    
                    private static Try<Integer> toInteger(boolean flag) {
                        return flag ? Try.success(1) : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_expression.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        label:
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Try.success("Alex");
                            default -> {
                                yield Try.success("Alex");
                            }
                        };
                    }
                    
                    private static Try<Integer> toInteger(boolean flag) {
                        return flag ? Try.success(1) : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_ruleWithBlock_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> {
                                var name = getName().unwrap();
                                yield Try.success(name.toUpperCase());
                            }
                        };
                    }
                    
                    private static Try<String> getName() {
                        return Try.success("Alex");
                    }
                    
                    private static Integer toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_ruleWithSingleExpression_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> Try.success(getName().unwrap().toUpperCase());
                        };
                    }
                    
                    private static Try<String> getName() {
                        return Try.success("Alex");
                    }
                    
                    private static Integer toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_statementCasesWithUnwrap_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default : {
                                var name = Try.success("Alex").unwrap();
                                yield Try.success(name);
                            }
                        };
                    }
                    
                    private static Integer toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
