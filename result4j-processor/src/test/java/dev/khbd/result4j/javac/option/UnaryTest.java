package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
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
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> getName(boolean flag) {
                        var notFlag = !flag(flag).unwrap();
                    
                        if (notFlag) {
                            return Option.none();
                        }
                        
                        return Option.some("Alex");
                    }
                    
                    public static Option<Boolean> flag(boolean flag) {
                        return Option.some(flag);
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
        Option<String> name = (Option<String>) method.invoke(null, false);
        assertThat(name.isEmpty()).isTrue();

        // call with true
        name = (Option<String>) method.invoke(null, true);
        assertThat(name.isEmpty()).isFalse();
        assertThat(name.get()).isEqualTo("Alex");
    }

}
