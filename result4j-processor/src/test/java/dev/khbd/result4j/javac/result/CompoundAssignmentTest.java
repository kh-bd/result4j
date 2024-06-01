package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
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
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, Integer> getSize(boolean flag) {
                        int result = 0;
                        
                        result += baseSize(flag).unwrap();
                        
                        return Result.success(result);
                    }
                                
                    private static Result<String, Integer> baseSize(boolean flag) {
                        return flag ? Result.success(10) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/compound_assignment/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.compound_assignment.Main");
        Method method = clazz.getMethod("getSize", boolean.class);

        // invoke with true
        Result<String, Integer> greet = (Result<String, Integer>) method.invoke(null, true);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo(10);

        // invoke with false
        greet = (Result<String, Integer>) method.invoke(null,false);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isEqualTo("error");
    }
}
