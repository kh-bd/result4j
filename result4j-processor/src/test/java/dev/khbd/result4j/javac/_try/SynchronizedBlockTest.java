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
public class SynchronizedBlockTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInBlock() throws Exception {
        String source = """
                package cases.sync_block;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        synchronized (Main.class) {
                            var name = getName(flag).unwrap();
                            return Try.success(name.toUpperCase());
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        return flag ? Try.success("Alex") : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.sync_block.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with false
        Try<String> greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

//      invoke with true
        greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInSyncExpression() throws Exception {
        String source = """
                package cases.sync_block;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        synchronized (getName(flag).unwrap()) {
                            return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        return flag ? Try.success("Alex") : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.sync_block.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with false
        Try<String> greet = (Try<String>) method.invoke(null, false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledSyncExpression() {
        String source = """
                package cases.sync_block;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        label:
                        synchronized (getName(flag).unwrap()) {
                            return Try.failure(new RuntimeException("error"));
                        }
                    }
                    
                    private static Try<String> getName(boolean flag) {
                        return flag ? Try.success("Alex") : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
