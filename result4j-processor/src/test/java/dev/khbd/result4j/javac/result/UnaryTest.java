package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class UnaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inUnaryNegationExpression() throws Exception {
        String source = """
                package cases.in_unary;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, String> getName(boolean flag) {
                        var notFlag = !flag(flag).unwrap();
                    
                        if (notFlag) {
                            return Result.error("error");
                        }
                        
                        return Result.success("Alex");
                    }
                    
                    public static Result<String, Boolean> flag(boolean flag) {
                        return Result.success(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_unary/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_unary.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Result<String, String> name = (Result<String, String>) method.invoke(null, false);
        assertThat(name.isError()).isTrue();
        assertThat(name.getError()).isEqualTo("error");

        // call with true
        name = (Result<String, String>) method.invoke(null, true);
        assertThat(name.isSuccess()).isTrue();
        assertThat(name.get()).isEqualTo("Alex");
    }

}
