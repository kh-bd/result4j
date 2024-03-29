package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ArrayAccessTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtInstancePosition() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static Either<String, String> greet(boolean flag) {
                        var name = getArray(flag).unwrap()[0];
                        return Either.right(name);
                    }
                    
                    private static Either<String, String[]> getArray(boolean flag) {
                        return flag ? Either.right(new String[] {"Alex"}) : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallAtIndexPosition() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static Either<String, String> greet(boolean flag) {
                        var names = new String[] {"Alex"};
                        var name = names[getIndex(flag).unwrap()];
                        return Either.right(name);
                    }
                    
                    private static Either<String, Integer> getIndex(boolean flag) {
                        return flag ? Either.right(0) : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallAtInstanceAndIndexPositions() throws Exception {
        String source = """
                package cases.array_access;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                
                    public static Either<String, String> greet(boolean flag) {
                        var name = getArray(flag).unwrap()[getIndex(flag).unwrap()];
                        return Either.right(name);
                    }
                    
                    private static Either<String, String[]> getArray(boolean flag) {
                        return flag ? Either.right(new String[] {"Alex"}) : Either.left("error");
                    }
                    
                    private static Either<String, Integer> getIndex(boolean flag) {
                        return flag ? Either.right(0) : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/array_access/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.array_access.Main");
        Method method = clazz.getMethod("greet", boolean.class);

        // invoke with true
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null, false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }
}
