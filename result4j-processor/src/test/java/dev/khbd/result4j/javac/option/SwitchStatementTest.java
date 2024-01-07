package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class SwitchStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReceiverExpression() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        switch(toInteger(flag).unwrap()) {
                            case 1:
                            default:
                                return Option.some("Alex");
                        }
                    }
                    
                    private static Option<Integer> toInteger(boolean flag) {
                        return flag ? Option.some(1) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/switch_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.switch_statement.Main");
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
    public void propagate_unwrapCallInRuleWithBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1 -> {
                                var name = getName(flag).unwrap();
                                return Option.some(name);
                            }
                            default -> {
                                return Option.none();
                            }
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInRuleWithOneStatement() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1 -> throw error(flag).unwrap();
                            default -> {
                                return Option.none();
                            }
                        }
                    }
                    
                    private static Option<RuntimeException> error(boolean flag) {
                        if (flag) {
                            return Option.some(new RuntimeException("Alex"));
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithoutBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1:
                                var name1 = getName(flag).unwrap();
                                return Option.some(name1);
                            default:
                                return Option.none();
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInCaseStatementWithoutBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1:
                                return Option.some(getName(flag).unwrap());
                            default:
                                return Option.none();
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInStatementCaseWithBlock() throws Exception {
        String source = """
                package cases.switch_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        switch(toInteger(flag)) {
                            case 1: {
                                var name1 = getName(flag).unwrap();
                                return Option.some(name1);
                            }
                            default:
                                return Option.none();
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }
}
