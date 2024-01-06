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
public class IfTest extends AbstractPluginTest {

    @Test
    public void propagate_inCondition_failCompilation() {
        String source = """
                package cases.if_statement;
                                
                import java.util.Random;
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName() {
                        if (random().unwrap().booleanValue()) {
                            return Option.some("Alex");
                        } else {
                            return Option.none();
                        }
                    }
                    
                    public static Option<Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Option.some(rnd.nextBoolean());
                        }
                        return Option.none();
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
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName() {
                        if (false) {
                            return Option.some("Alex");
                        } else if (random().unwrap().booleanValue()) {
                            return Option.some("Sergei");
                        } else {
                            return Option.none();
                        }
                    }
                    
                    public static Option<Boolean> random() {
                        var rnd = new Random();
                        if (rnd.nextBoolean()) {
                            return Option.some(rnd.nextBoolean());
                        }
                        return Option.none();
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some(getName().unwrap().toUpperCase());
                        } else {
                            return Option.none();
                        }
                    }
                                
                    public static Option<String> getName() {
                        return Option.some("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Option<String> name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();
    }

    @Test
    public void propagate_inElseBlock_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.none();
                        } else {
                            return Option.some(getName().unwrap().toUpperCase());
                        }
                    }
                                
                    public static Option<String> getName() {
                        return Option.some("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isTrue();
    }

    @Test
    public void propagate_inThenStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName(boolean flag) {
                        if (flag) return Option.some(getName().unwrap().toUpperCase());
                        else return Option.none();
                    }
                                
                    public static Option<String> getName() {
                        return Option.some("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with true
        Option<String> name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with false
        name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();
    }

    @Test
    public void propagate_inElseStatement_success() throws Exception {
        String source = """
                package cases.if_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName(boolean flag) {
                        if (flag) return Option.none();
                        else return Option.some(getName().unwrap().toUpperCase());
                    }
                                
                    public static Option<String> getName() {
                        return Option.some("Alex");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/if_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.if_statement.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("ALEX");

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isTrue();
    }
}
