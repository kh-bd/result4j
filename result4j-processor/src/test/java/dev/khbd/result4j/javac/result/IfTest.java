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
    public void propagate_inCondition() throws Exception {
        String source = """
                package cases.if_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> getName(int flag) {
                        if (getFlag(flag).unwrap().booleanValue()) {
                            return Result.success("Alex");
                        }
                        return Result.success("Sergei");
                    }
                
                    public static Result<String, Boolean> getFlag(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        }
                        return Result.success(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", int.class);

        // call with 0
        Result<String, String> name = (Result<String, String>) method.invoke(null, 0);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with positive number
        name = (Result<String, String>) method.invoke(null, 10);
        assertThat(name.isError()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");

        // call with negative number
        name = (Result<String, String>) method.invoke(null, -10);
        assertThat(name.isError()).isFalse();
        assertThat(name.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_inComplexConditionButAtLeftSide() throws Exception {
        String source = """
                package cases.if_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> getName(int flag) {
                        if (getFlag(flag).unwrap().booleanValue() && flag > 0) {
                            return Result.success("Alex");
                        }
                        return Result.success("Sergei");
                    }
                
                    public static Result<String, Boolean> getFlag(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        }
                        return Result.success(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", int.class);

        // call with 0
        Result<String, String> name = (Result<String, String>) method.invoke(null, 0);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with positive number
        name = (Result<String, String>) method.invoke(null, 10);
        assertThat(name.isError()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");

        // call with negative number
        name = (Result<String, String>) method.invoke(null, -10);
        assertThat(name.isError()).isFalse();
        assertThat(name.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_inElseIfCondition_failCompilation() throws Exception {
        String source = """
                package cases.if_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> getName(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        } else if (getFlag(flag).unwrap().booleanValue()) {
                            return Result.success("Alex");
                        }
                        return Result.success("Sergei");
                    }
                
                    public static Result<String, Boolean> getFlag(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        }
                        return Result.success(flag > 0);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isFail()).isFalse();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", int.class);

        // call with 0
        Result<String, String> name = (Result<String, String>) method.invoke(null, 0);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with positive number
        name = (Result<String, String>) method.invoke(null, 10);
        assertThat(name.isError()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");

        // call with negative number
        name = (Result<String, String>) method.invoke(null, -10);
        assertThat(name.isError()).isFalse();
        assertThat(name.get()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_inRightSideOfComplexCondition_failCompilation() {
        String source = """
                package cases.if_statement;
                
                import dev.khbd.result4j.core.Result;
                
                public class Main {
                
                    public static Result<String, String> getName(int flag) {
                        if (flag > 0 && getFlag(flag).unwrap().booleanValue()) {
                            return Result.success("Alex");
                        }
                        return Result.error("error");
                    }
                
                    public static Result<String, Boolean> getFlag(int flag) {
                        if (flag == 0) {
                            return Result.error("error");
                        }
                        return Result.success(flag > 0);
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
