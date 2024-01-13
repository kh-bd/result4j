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
public class TryTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInResources_failToCompile() {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Try;
                import java.lang.AutoCloseable;
                
                                
                public class Main {
                                
                    public static Try<?> greet(boolean flag) {
                        try (var name = getName(flag).unwrap()) {
                            return Try.success(name.name);
                        }
                    }
                    
                    private static Try<Name> getName(boolean flag) {
                        if (flag) {
                            return Try.success(new Name("Alex"));
                        }
                        return Try.failure(new RuntimeException("error"));
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
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        try {
                            var name = getName(flag).unwrap();
                            return Try.success(name);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        System.out.println(result);
        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } catch (Exception e) {
                            return Try.success(getName(flag).unwrap());
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        System.out.println(result);
        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } finally {
                            var name = getName(flag).unwrap();
                            return Try.success(name);
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success("Alex");
                        }
                        return Try.failure(new RuntimeException("error"));
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        System.out.println(result);
        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
