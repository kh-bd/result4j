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
public class TryTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInResources_failToCompile() {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Option;
                import java.lang.AutoCloseable;
                
                                
                public class Main {
                                
                    public static Option<?> greet(boolean flag) {
                        try (var name = getName(flag).unwrap()) {
                            return Option.some(name.name);
                        }
                    }
                    
                    private static Option<Name> getName(boolean flag) {
                        if (flag) {
                            return Option.some(new Name("Alex"));
                        }
                        return Option.none();
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(boolean flag) {
                        try {
                            var name = getName(flag).unwrap();
                            return Option.some(name);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return Option.none();
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInCatchBlock() throws Exception {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } catch (Exception e) {
                            return Option.some(getName(flag).unwrap());
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }

    @Test
    public void propagate_unwrapCallInFinallyBlock() throws Exception {
        String source = """
                package cases.try_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(boolean flag) {
                        try {
                            throw new RuntimeException();
                        } finally {
                            var name = getName(flag).unwrap();
                            return Option.some(name);
                        }
                    }
                    
                    private static Option<String> getName(boolean flag) {
                        if (flag) {
                            return Option.some("Alex");
                        }
                        return Option.none();
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
        Option<String> greet = (Option<String>) method.invoke(null, true);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Option<String>) method.invoke(null, false);
        assertThat(greet.isEmpty()).isTrue();
    }
}
