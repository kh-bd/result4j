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
public class IfTest extends AbstractPluginTest {

    @Test
    public void propagate_inCondition_failCompilation() {
        String source = """
                package cases.if_statement;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName() {
                        if (random().unwrap().booleanValue()) {
                            return Either.right("Alex");
                        } else {
                            return Either.left("error");
                        }
                    }
                    
                    public static Either<String, Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextBoolean());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }


    @Test
    public void propagate_inElseIfCondition_failCompilation() {
        String source = """
                package cases.if_statement;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName() {
                        if (false) {
                            return Either.right("Alex");
                        } else if (random().unwrap().booleanValue()) {
                            return Either.right("Sergei");
                        } else {
                            return Either.left("error");
                        }
                    }
                    
                    public static Either<String, Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Either.right(rnd.nextBoolean());
                        }
                        return Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inThenBlock_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.right(getName().unwrap().toUpperCase());
                        } else {
                            return Either.left("error");
                        }
                    }
                                
                    public static Either<String, String> getName() {
                        return Either.right("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Either<String, String> name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with false
        name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) {
                        if (flag) {
                            return Either.left("error");
                        } else {
                            return Either.right(getName().unwrap().toUpperCase());
                        }
                    }
                                
                    public static Either<String, String> getName() {
                        return Either.right("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) {
                        if (flag) return Either.right(getName().unwrap().toUpperCase());
                        else return Either.left("error");
                    }
                                
                    public static Either<String, String> getName() {
                        return Either.right("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Either<String, String> name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with false
        name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) {
                        if (flag) return Either.left("error");
                        else return Either.right(getName().unwrap().toUpperCase());
                    }
                                
                    public static Either<String, String> getName() {
                        return Either.right("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");
    }
}
