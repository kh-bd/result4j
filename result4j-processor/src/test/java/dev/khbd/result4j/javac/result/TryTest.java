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
public class TryTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInResourcesWithOneBlock() throws Exception {
        String source = """
                package cases.try_statement;
                
                import dev.khbd.result4j.core.Result;
                import java.lang.AutoCloseable;
                
                public class Main {
                
                    public static Result<String, ?> greet(boolean flag) {
                        try (var name = getName(flag).unwrap()) {
                            return Result.success(name.name);
                        }
                    }
                
                    private static Result<String, Name> getName(boolean flag) {
                        if (flag) {
                            return Result.success(new Name("Alex"));
                        }
                        return Result.error("error");
                    }
                }
                
                class Name implements AutoCloseable {
                    String name;
                
                    Name(String name) {
                        this.name = name;
                    }
                
                    public void close() {}
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
    public void propagate_unwrapCallInResourcesWithSeveralBlocksButUnwrapAtFirstPosition() throws Exception {
        String source = """
                package cases.try_statement;
                
                import dev.khbd.result4j.core.Result;
                import java.lang.AutoCloseable;
                
                public class Main {
                
                    public static Result<String, ?> greet(boolean flag) {
                        try (var name = getName(flag).unwrap(); var name2 = new Name("Alex")) {
                            return Result.success(name.name);
                        }
                    }
                
                    private static Result<String, Name> getName(boolean flag) {
                        if (flag) {
                            return Result.success(new Name("Alex"));
                        }
                        return Result.error("error");
                    }
                }
                
                class Name implements AutoCloseable {
                    String name;
                
                    Name(String name) {
                        this.name = name;
                    }
                
                    public void close() {}
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
    public void propagate_unwrapCallInResourcesWithSeveralBlocksButUnwrapAtSecondPosition_failToCompile() throws Exception {
        String source = """
                package cases.try_statement;
                
                import dev.khbd.result4j.core.Result;
                import java.lang.AutoCloseable;
                
                public class Main {
                
                    public static Result<String, ?> greet(boolean flag) {
                        try (var name = getName(flag).unwrap(); var name2 = getName(!flag).unwrap()) {
                            return Result.success(name.name);
                        }
                    }
                
                    private static Result<String, Name> getName(boolean flag) {
                        if (flag) {
                            return Result.success(new Name("Alex"));
                        }
                        return Result.error("error");
                    }
                }
                
                class Name implements AutoCloseable {
                    String name;
                
                    Name(String name) {
                        this.name = name;
                    }
                
                    public void close() {}
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_unwrapCallInTryBlock() throws Exception {
        String source = """
                package cases.try_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> greet(boolean flag) {
                        try {
                            var name = getName(flag).unwrap();
                            return Result.success(name);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Result.error("error");
                        }
                    }
                
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source = """
                package cases.try_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } catch (Exception e) {
                            return Result.success(getName(flag).unwrap());
                        }
                    }
                
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source = """
                package cases.try_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } finally {
                            var name = getName(flag).unwrap();
                            return Result.success(name);
                        }
                    }
                
                    private static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
