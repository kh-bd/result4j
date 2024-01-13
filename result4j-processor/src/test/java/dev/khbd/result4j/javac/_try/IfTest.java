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
public class IfTest extends AbstractPluginTest {

    @Test
    public void propagate_inCondition_failCompilation() {
        String source = """
                package cases.if_statement;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName() {
                        if (random().unwrap().booleanValue()) {
                            return Try.success("Alex");
                        } else {
                            return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    public static Try<Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Try.success(rnd.nextBoolean());
                        }
                        return Try.failure(new RuntimeException("error"));
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
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName() {
                        if (false) {
                            return Try.success("Alex");
                        } else if (random().unwrap().booleanValue()) {
                            return Try.success("Sergei");
                        } else {
                            return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    public static Try<Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Try.success(rnd.nextBoolean());
                        }
                        return Try.failure(new RuntimeException("error"));
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
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.success(getName().unwrap().toUpperCase());
                        } else {
                            return Try.failure(new RuntimeException("error"));
                        }
                    }
                                
                    public static Try<String> getName() {
                        return Try.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Try<String> name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName(boolean flag) {
                        if (flag) {
                            return Try.failure(new RuntimeException("error"));
                        } else {
                            return Try.success(getName().unwrap().toUpperCase());
                        }
                    }
                                
                    public static Try<String> getName() {
                        return Try.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName(boolean flag) {
                        if (flag) return Try.success(getName().unwrap().toUpperCase());
                        else return Try.failure(new RuntimeException("error"));
                    }
                                
                    public static Try<String> getName() {
                        return Try.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Try<String> name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> getName(boolean flag) {
                        if (flag) return Try.failure(new RuntimeException("error"));
                        else return Try.success(getName().unwrap().toUpperCase());
                    }
                                
                    public static Try<String> getName() {
                        return Try.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Try<String> name = (Try<String>) method.invoke(null, false);
        assertThat(name.isFailure()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Try<String>) method.invoke(null, true);
        assertThat(name.isFailure()).isTrue();
        assertThat(name.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
