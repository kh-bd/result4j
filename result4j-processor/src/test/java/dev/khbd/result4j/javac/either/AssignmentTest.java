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
public class AssignmentTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInAssignment() throws Exception {
        String source = """
                package cases.assignment;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        String name = null;
                        name = name(flag).unwrap();
                        return Either.right(name);
                    }
                                
                    private static Either<String, String> name(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
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
        Either<String, String> greet = (Either<String, String>) method.invoke(null, true);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("Alex");

        // invoke with false
        greet = (Either<String, String>) method.invoke(null,false);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isEqualTo("error");
    }

    @Test
    public void propagate_unwrapCallInLabeledAssignment() {
        String source = """
                package cases.assignment;
                                
                import dev.khbd.result4j.core.Either;
                                
                public class Main {
                                
                    public static Either<String, String> greet(boolean flag) {
                        String name = null;
                        label:
                        name = name(flag).unwrap();
                        return Either.right(name);
                    }
                                
                    private static Either<String, String> name(boolean flag) {
                        return flag ? Either.right("Alex") : Either.left("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/labeled/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
