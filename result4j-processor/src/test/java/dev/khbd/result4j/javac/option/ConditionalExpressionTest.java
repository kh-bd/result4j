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
public class ConditionalExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_nestedThen_failCompilation() {
        String source = """
                package cases.conditional_expression;
                
                import java.util.Random;
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    static Random rnd = new Random();
                
                    public static Option<String> getName() {
                        return rnd.nextBoolean()
                          ? (random().unwrap() ? Option.some("Alex") : Option.none())
                          : Option.some("Alex");
                    }
                
                    public static Option<Boolean> random() {
                        if (rnd.nextBoolean()) {
                            return Option.some(rnd.nextBoolean());
                        }
                        return Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_nestedElse_failCompilation() {
        String source = """
                package cases.conditional_expression;
                
                import java.util.Random;
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    static Random rnd = new Random();
                
                    public static Option<String> getName() {
                        return rnd.nextBoolean()
                          ? Option.some("Alex")
                          : (random().unwrap() ? Option.some("Alex") : Option.none());
                    }
                
                    public static Option<Boolean> random() {
                        if (rnd.nextBoolean()) {
                            return Option.some(rnd.nextBoolean());
                        }
                        return Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_nestedCondition() throws Exception {
        String source = """
                package cases.conditional_expression;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<String> getName(int flag) {
                        return (flag(flag).unwrap() ? true : false)
                          ? Option.some("Alex")
                          : Option.some("Sergei");
                    }
                
                    public static Option<Boolean> flag(int flag) {
                        if (flag == 0) {
                            return Option.none();
                        }
                        return Option.some(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Option<String> unwrapped = (Option<String>) method.invoke(null, 0);
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, 10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Option<String>) method.invoke(null, -10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInCondition() throws Exception {
        String source = """
                package cases.conditional_expression;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<String> getName(int flag) {
                        return flag(flag).unwrap()
                          ? Option.some("Alex")
                          : Option.some("Sergei");
                    }
                
                    public static Option<Boolean> flag(int flag) {
                        if (flag == 0) {
                            return Option.none();
                        }
                        return Option.some(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Option<String> unwrapped = (Option<String>) method.invoke(null, 0);
        assertThat(unwrapped.isEmpty()).isTrue();

        unwrapped = (Option<String>) method.invoke(null, 10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Option<String>) method.invoke(null, -10);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
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

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
