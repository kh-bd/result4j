package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Option.some("Alex");
                            default -> {
                                yield Option.some("Alex");
                            }
                        };
                    }
                    
                    private static Option<Integer> toInteger(boolean flag) {
                        return flag ? Option.some(1) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_expression.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        label:
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Option.some("Alex");
                            default -> {
                                yield Option.some("Alex");
                            }
                        };
                    }
                    
                    private static Option<Integer> toInteger(boolean flag) {
                        return flag ? Option.some(1) : Option.none();
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> {
                                var name = getName().unwrap();
                                yield Option.some(name.toUpperCase());
                            }
                        };
                    }
                    
                    private static Option<String> getName() {
                        return Option.some("Alex");
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> Option.some(getName().unwrap().toUpperCase());
                        };
                    }
                    
                    private static Option<String> getName() {
                        return Option.some("Alex");
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default : {
                                var name = Option.some("Alex").unwrap();
                                yield Option.some(name);
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
