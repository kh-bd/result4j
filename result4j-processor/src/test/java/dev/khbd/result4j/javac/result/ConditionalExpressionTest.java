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
public class ConditionalExpressionTest extends AbstractPluginTest {

    @Test
    public void propagate_nestedThen_failCompilation() {
        String source = """
                package cases.conditional_expression;
                
                import java.util.Random;
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    static Random rnd = new Random();
                
                    public static Result<String, String> getName() {
                        return rnd.nextBoolean()
                          ? (random().unwrap() ? Result.success("Alex") : Result.error("error"))
                          : Result.success("Alex");
                    }
                
                    public static Result<String, Boolean> random() {
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
    public void propagate_nestedElse_failCompilation() {
        String source = """
                package cases.conditional_expression;
                
                import java.util.Random;
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    static Random rnd = new Random();
                
                    public static Result<String, String> getName() {
                        return rnd.nextBoolean()
                          ? Result.success("Alex")
                          : (random().unwrap() ? Result.success("Alex") : Result.error("error"));
                    }
                
                    public static Result<String, Boolean> random() {
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
    public void propagate_nestedCondition() throws Exception {
        String source = """
                package cases.conditional_expression;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> getName(int flag) {
                        return (flag(flag).unwrap() ? true : false)
                          ? Result.success("Alex")
                          : Result.success("Sergei");
                    }
                
                    public static Result<String, Boolean> flag(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        }
                        return Result.success(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Result<String, String> unwrapped = (Result<String, String>) method.invoke(null, 0);
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<String, String>) method.invoke(null, 10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Result<String, String>) method.invoke(null, -10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInCondition() throws Exception {
        String source = """
                package cases.conditional_expression;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> getName(int flag) {
                        return flag(flag).unwrap()
                          ? Result.success("Alex")
                          : Result.success("Sergei");
                    }
                
                    public static Result<String, Boolean> flag(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        }
                        return Result.success(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/conditional_expression/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.conditional_expression.Main");
        Method method = clazz.getMethod("getName", int.class);

        Result<String, String> unwrapped = (Result<String, String>) method.invoke(null, 0);
        assertThat(unwrapped.isError()).isTrue();
        assertThat(unwrapped.getError()).isEqualTo("error");

        unwrapped = (Result<String, String>) method.invoke(null, 10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Alex");

        unwrapped = (Result<String, String>) method.invoke(null, -10);
        assertThat(unwrapped.isSuccess()).isTrue();
        assertThat(unwrapped.get()).isEqualTo("Sergei");
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
