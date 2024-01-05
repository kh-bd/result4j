package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class ExpressionStatementTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallOnExpressionStatement_propagate() throws Exception {
        String source = """
                package cases.expression_statement;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(int index) {
                        // result is ignored
                        name(index).unwrap();
                        return Option.some("Alex");
                    }
                                
                    private static Option<String> name(int index) {
                        if (index == 0) {
                            return Option.none();
                        }
                        return Option.some("Sergei");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/expression_statement/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.expression_statement.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with not 0
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");
    }
}
