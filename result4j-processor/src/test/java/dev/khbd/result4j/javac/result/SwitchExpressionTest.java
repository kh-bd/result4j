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
public class SwitchExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Result.success("Alex");
                            default -> {
                                yield Result.success("Alex");
                            }
                        };
                    }
                    
                    private static Result<String, Integer> toInteger(boolean flag) {
                        return flag ? Result.success(1) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_expression.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        label:
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Result.success("Alex");
                            default -> {
                                yield Result.success("Alex");
                            }
                        };
                    }
                    
                    private static Result<String, Integer> toInteger(boolean flag) {
                        return flag ? Result.success(1) : Result.error("error");
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> {
                                var name = getName().unwrap();
                                yield Result.success(name.toUpperCase());
                            }
                        };
                    }
                    
                    private static Result<String, String> getName() {
                        return Result.success("Alex");
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> Result.success(getName().unwrap().toUpperCase());
                        };
                    }
                    
                    private static Result<String, String> getName() {
                        return Result.success("Alex");
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default : {
                                var name = Result.success("Alex").unwrap();
                                yield Result.success(name);
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
