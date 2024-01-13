package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class SwitchStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        switch(toInteger(flag).unwrap()) {
                            case 1:
                            default:
                                return Try.success("Alex");
                        }
                    }
                    
                    private static Try<Integer> toInteger(boolean flag) {
                        return flag ? Try.success(1) : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        label:
                        switch(toInteger(flag).unwrap()) {
                            case 1:
                            default:
                                return Try.success("Alex");
                        }
                    }
                    
                    private static Try<Integer> toInteger(boolean flag) {
                        return flag ? Try.success(1) : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/labeled/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInRuleWithBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1 -> {
                                var name = getName(flag).unwrap();
                                return Try.success(name);
                            }
                            default -> {
                                return Try.failure(new RuntimeException("error"));
                            }
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                    
                    private static int toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
    public void propagate_unwrapCallInRuleWithOneStatement() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1 -> throw error(flag).unwrap();
                            default -> {
                                return Try.failure(new RuntimeException("error"));
                            }
                        }
                    }
                    
                    private static Try<RuntimeException> error(boolean flag) {
                        if (flag) {
                            return Try.success(new RuntimeException("Alex"));
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                    
                    private static int toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Throwable error = catchThrowable(() -> method.invoke(null, true));
        assertThat(error)
                .cause()
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Alex");

        // invoke with false
        Try<String> greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithoutBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1:
                                var name1 = getName(flag).unwrap();
                                return Try.success(name1);
                            default:
                                return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                    
                    private static int toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
    public void propagate_unwrapCallInCaseStatementWithoutBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1:
                                return Try.success(getName(flag).unwrap());
                            default:
                                return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                    
                    private static int toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
    public void propagate_unwrapCallInStatementCaseWithBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1: {
                                var name1 = getName(flag).unwrap();
                                return Try.success(name1);
                            }
                            default:
                                return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                    
                    private static int toInteger(boolean flag) {
                        return flag ? 1 : 0;
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
}
