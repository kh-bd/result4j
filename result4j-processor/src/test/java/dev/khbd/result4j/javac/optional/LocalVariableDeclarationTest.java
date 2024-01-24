package dev.khbd.result4j.javac.optional;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;
import java.util.Optional;

/**
 * @author Sergei_Khadanovich
 */
public class LocalVariableDeclarationTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapOnLocalVarInitExpression_propagate() throws Exception {
        String source = """
                package cases.local_variable_declaration;
                
                import java.util.Optional;
                                
                public class Main {
                                
                    public static Optional<String> greet(int index) {
                        var name = getName(index).unwrap();
                        return Optional.of(name.toUpperCase());
                    }
                                
                    private static Optional<String> getName(int index) {
                        if (index == 0) {
                            return Optional.empty();
                        }
                        return Optional.of(index < 0 ? "Alex" : "Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/local_variable_declaration/Main.java", source);

        System.out.println(result);
        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.local_variable_declaration.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Optional<String> greet = (Optional<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with negative
        greet = (Optional<String>) method.invoke(null, -10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("ALEX");

        // invoke with positive
        greet = (Optional<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("SERGEI");
    }
}
