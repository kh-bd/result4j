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
public class IfTest extends AbstractPluginTest {

    @Test
    public void propagate_inCondition_failCompilation() {
        String source = """
                package cases.if_statement;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName() {
                        if (random().unwrap().booleanValue()) {
                            return Result.success("Alex");
                        } else {
                            return Result.error("error");
                        }
                    }
                    
                    public static Result<String, Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Result.success(rnd.nextBoolean());
                        }
                        return Result.error("error");
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
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName() {
                        if (false) {
                            return Result.success("Alex");
                        } else if (random().unwrap().booleanValue()) {
                            return Result.success("Sergei");
                        } else {
                            return Result.error("error");
                        }
                    }
                    
                    public static Result<String, Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Result.success(rnd.nextBoolean());
                        }
                        return Result.error("error");
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.success(getName().unwrap().toUpperCase());
                        } else {
                            return Result.error("error");
                        }
                    }
                                
                    public static Result<String, String> getName() {
                        return Result.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Result<String, String> name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName(boolean flag) {
                        if (flag) {
                            return Result.error("error");
                        } else {
                            return Result.success(getName().unwrap().toUpperCase());
                        }
                    }
                                
                    public static Result<String, String> getName() {
                        return Result.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Result<String, String> name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName(boolean flag) {
                        if (flag) return Result.success(getName().unwrap().toUpperCase());
                        else return Result.error("error");
                    }
                                
                    public static Result<String, String> getName() {
                        return Result.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Result<String, String> name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName(boolean flag) {
                        if (flag) return Result.error("error");
                        else return Result.success(getName().unwrap().toUpperCase());
                    }
                                
                    public static Result<String, String> getName() {
                        return Result.success("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Result<String, String> name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");
    }
}
