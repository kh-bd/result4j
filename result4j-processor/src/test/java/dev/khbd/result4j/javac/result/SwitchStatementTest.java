package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Result;
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        switch(toInteger(flag).unwrap()) {
                            case 1:
                            default:
                                return Result.success("Alex");
                        }
                    }
                    
                    private static Result<String, Integer> toInteger(boolean flag) {
                        return flag ? Result.success(1) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        label:
                        switch(toInteger(flag).unwrap()) {
                            case 1:
                            default:
                                return Result.success("Alex");
                        }
                    }
                    
                    private static Result<String, Integer> toInteger(boolean flag) {
                        return flag ? Result.success(1) : Result.error("error");
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1 -> {
                                var name = getName(flag).unwrap();
                                return Result.success(name);
                            }
                            default -> {
                                return Result.error("error");
                            }
                        }
                    }
                    
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
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
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInRuleWithOneStatement() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1 -> throw error(flag).unwrap();
                            default -> {
                                return Result.error("error");
                            }
                        }
                    }
                    
                    private static Result<String, RuntimeException> error(boolean flag) {
                        if (flag) {
                            return Result.success(new RuntimeException("Alex"));
                        }
                        return Result.error("error");
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
        Result<String, String> greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithoutBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1:
                                var name1 = getName(flag).unwrap();
                                return Result.success(name1);
                            default:
                                return Result.error("error");
                        }
                    }
                    
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
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
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInCaseStatementWithoutBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1:
                                return Result.success(getName(flag).unwrap());
                            default:
                                return Result.error("error");
                        }
                    }
                    
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
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
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, ?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1: {
                                var name1 = getName(flag).unwrap();
                                return Result.success(name1);
                            }
                            default:
                                return Result.error("error");
                        }
                    }
                    
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
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
        Result<String, String> greet = (Result<String, String>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Result<String, String>) method.invoke(null, false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }
}
