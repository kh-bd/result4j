package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class LambdaTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInLambdaBlock() throws Exception {
        String source = """
                package cases.in_lambda;
                                
                import dev.khbd.result4j.core.Either;
                import java.util.concurrent.Callable;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) throws Exception {
                        Callable<Either<String, String>> call = () -> {
                            var name = name(flag).unwrap();
                            return Either.right(name.toUpperCase());
                        };
                        return call.call();
                    }
                    
                    public static Either<String, String> name(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");
    }

    @Test
    public void propagate_unwrapCallInLambdaExpression() throws Exception {
        String source = """
                package cases.in_lambda;
                                
                import dev.khbd.result4j.core.Either;
                import java.util.concurrent.Callable;
                                
                public class Main {
                                
                    public static Either<String, String> getName(boolean flag) throws Exception {
                        Callable<Either<String, String>> call = () -> Either.right(name(flag).unwrap().toUpperCase());
                        return call.call();
                    }
                    
                    public static Either<String, String> name(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_lambda/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_lambda.Main");
        Method method = clazz.getMethod("getName", boolean.class);

        // call with false
        Either<String, String> name = (Either<String, String>) method.invoke(null, false);
        assertThat(name.isLeft()).isTrue();
        assertThat(name.getLeft()).isEqualTo("error");

        // call with true
        name = (Either<String, String>) method.invoke(null, true);
        assertThat(name.isRight()).isTrue();
        assertThat(name.getRight()).isEqualTo("ALEX");
    }

}
