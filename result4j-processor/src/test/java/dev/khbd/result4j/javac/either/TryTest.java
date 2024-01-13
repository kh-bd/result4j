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
public class TryTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInResources_failToCompile() {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Either;
                import java.lang.AutoCloseable;
                
                                
                public class Main {
                                
                    public static Either<String, ?> greet(boolean flag) {
                        try (var name = getName(flag).unwrap()) {
                            return Either.right(name.name);
                        }
                    }
                    
                    private static Either<String, Name> getName(boolean flag) {
                        if (flag) {
                            return Either.right(new Name("Alex"));
                        }
                        return Either.left("error");
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
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        try {
                            var name = getName(flag).unwrap();
                            return Either.right(name);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Either.left("error");
                        }
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.right("Alex");
                        }
                        return Either.left("error");
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
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } catch (Exception e) {
                            return Either.right(getName(flag).unwrap());
                        }
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.right("Alex");
                        }
                        return Either.left("error");
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
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } finally {
                            var name = getName(flag).unwrap();
                            return Either.right(name);
                        }
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.right("Alex");
                        }
                        return Either.left("error");
                    }
                }
                
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/try_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.try_statement.Main");
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
}
