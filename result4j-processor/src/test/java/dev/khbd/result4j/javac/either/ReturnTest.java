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
public class ReturnTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInReturn_propagate() throws Exception {
        String source = """
                package cases.in_return;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<RuntimeException, String> greet(int index) {
                        return Either.right(name(index).unwrap());
                    }
                                
                    private static Either<RuntimeException, String> name(int index) {
                        if (index == 0) {
                            return Either.left(new RuntimeException("error"));
                        }
                        return Either.right(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_return.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Either<Exception, String> greet = (Either<Exception, String>) method.invoke(null, 0);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");

        // invoke with negative
        greet = (Either<Exception, String>) method.invoke(null, -10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with positive
        greet = (Either<Exception, String>) method.invoke(null, 10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Sergei");
    }

    @Test
    public void propagate_unwrapCallInLabeledReturn_propagate() {
        String source = """
                package cases.in_return;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<RuntimeException, String> greet(int index) {
                        label:
                        return Either.right(name(index).unwrap());
                    }
                                
                    private static Either<RuntimeException, String> name(int index) {
                        if (index == 0) {
                            return Either.left(new RuntimeException("error"));
                        }
                        return Either.right(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_return/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
