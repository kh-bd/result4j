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
public class SwitchExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Either.right("Alex");
                            default -> {
                                yield Either.right("Alex");
                            }
                        };
                    }
                    
                    private static Either<String, Integer> toInteger(boolean flag) {
                        return flag ? Either.right(1) : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_expression/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_expression.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledReceiverExpression_fail() {
        String source = """
                package cases.switch_expression;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        label:
                        return switch(toInteger(flag).unwrap()) {
                            case 1 -> Either.right("Alex");
                            default -> {
                                yield Either.right("Alex");
                            }
                        };
                    }
                    
                    private static Either<String, Integer> toInteger(boolean flag) {
                        return flag ? Either.right(1) : Either.left("error");
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
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> {
                                var name = getName().unwrap();
                                yield Either.right(name.toUpperCase());
                            }
                        };
                    }
                    
                    private static Either<String, String> getName() {
                        return Either.right("Alex");
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
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default -> Either.right(getName().unwrap().toUpperCase());
                        };
                    }
                    
                    private static Either<String, String> getName() {
                        return Either.right("Alex");
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
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        return switch(toInteger(flag)) {
                            default : {
                                var name = Either.right("Alex").unwrap();
                                yield Either.right(name);
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
