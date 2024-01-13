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
public class AssignmentTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInAssignment() throws Exception {
        String source = """
                package cases.assignment;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        String name = null;
                        name = name(flag).unwrap();
                        return Try.success(name);
                    }
                                
                    private static Try<String> name(boolean flag) {
                        return flag ? Try.success("Alex") : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/assignment/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.assignment.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Try<String> greet = (Try<String>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with false
        greet = (Try<String>) method.invoke(null,false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledAssignment() {
        String source = """
                package cases.assignment;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<String> greet(boolean flag) {
                        String name = null;
                        label:
                        name = name(flag).unwrap();
                        return Try.success(name);
                    }
                                
                    private static Try<String> name(boolean flag) {
                        return flag ? Try.success("Alex") : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/labeled/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
