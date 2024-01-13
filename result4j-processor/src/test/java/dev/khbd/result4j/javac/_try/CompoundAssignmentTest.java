package dev.khbd.result4j.javac._try;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Try;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class CompoundAssignmentTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInAssignment() throws Exception {
        String source = """
                package cases.compound_assignment;
                                
                import dev.khbd.result4j.core.Try;
                                
                public class Main {
                                
                    public static Try<Integer> getSize(boolean flag) {
                        int result = 0;
                        
                        result += baseSize(flag).unwrap();
                        
                        return Try.success(result);
                    }
                                
                    private static Try<Integer> baseSize(boolean flag) {
                        return flag ? Try.success(10) : Try.failure(new RuntimeException("error"));
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/compound_assignment/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.compound_assignment.Main");
        Method method = clazz.getMethod("getSize", boolean.class);

        // invoke with true
        Try<Integer> greet = (Try<Integer>) method.invoke(null, true);
        assertThat(greet.isFailure()).isFalse();
        assertThat(greet.get()).isEqualTo(10);

        // invoke with false
        greet = (Try<Integer>) method.invoke(null,false);
        assertThat(greet.isFailure()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }
}
