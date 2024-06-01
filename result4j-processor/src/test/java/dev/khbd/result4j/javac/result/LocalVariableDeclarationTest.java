package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class LocalVariableDeclarationTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapOnLocalVarInitExpression_propagate() throws Exception {
        String source = """
                package cases.local_variable_declaration;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<Exception, String> greet(int index) {
                        var name = name(index).unwrap();
                        return Result.success(name.toUpperCase());
                    }
                                
                    private static Result<Exception, String> name(int index) {
                        if (index == 0) {
                            return Result.error(new RuntimeException("ERROR"));
                        }
                        return Result.success(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/local_variable_declaration/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.local_variable_declaration.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Result<Exception, String> greet = (Result<Exception, String>) method.invoke(null, 0);
        assertThat(greet.isError()).isTrue();
        assertThat(greet.getError()).isInstanceOf(RuntimeException.class)
                .hasMessage("ERROR");

        // invoke with negative
        greet = (Result<Exception, String>) method.invoke(null, -10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("ALEX");

        // invoke with positive
        greet = (Result<Exception, String>) method.invoke(null, 10);
        assertThat(greet.isSuccess()).isTrue();
        assertThat(greet.get()).isEqualTo("SERGEI");
    }
}
