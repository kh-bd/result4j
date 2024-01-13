package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
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
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) {
                        var notFlag = !flag(flag).unwrap();
                    
                        if (notFlag) {
                            return Either.left("error");
                        }
                        
                        return Either.right("Alex");
                    }
                    
                    public static Either<String, Boolean> flag(boolean flag) {
                        return Either.right(flag);
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
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("Alex");
    }

}
