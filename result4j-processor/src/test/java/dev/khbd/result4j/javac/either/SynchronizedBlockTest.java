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
public class SynchronizedBlockTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInBlock() throws Exception {
        String source = """
                package cases.sync_block;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        synchronized (Main.class) {
                            var name = getName(flag).unwrap();
                            return Either.right(name.toUpperCase());
                        }
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.sync_block.Main");
        Method method = clazz.getMethod("greet", boolean.class);

//      invoke with false
        Either<String, String> greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");

//      invoke with true
        greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInSyncExpression() throws Exception {
        String source = """
                package cases.sync_block;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        synchronized (getName(flag).unwrap()) {
                            return Either.left("error");
                        }
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
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
        Either<String, String> greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledSyncExpression() {
        String source = """
                package cases.sync_block;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        label:
                        synchronized (getName(flag).unwrap()) {
                            return Either.left("error");
                        }
                    }
                    
                    private static Either<String, String> getName(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/sync_block/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
