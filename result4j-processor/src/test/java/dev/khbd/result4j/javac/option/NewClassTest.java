package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class NewClassTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallAtArgumentPosition_propagate() throws Exception {
        String source = """
                package cases.new_class;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<String> greet(int index) {
                        var name = new Name(getName(index).unwrap(), getName(index).unwrap());
                        return Option.some(name.name);
                    }
                                
                    private static Option<String> getName(int index) {
                        if (index == 0) {
                            return Option.none();
                        }
                        return Option.some(index < 0 ? "Alex" : "Sergei");
                    }
                    
                    static class Name {
                        String name;
                        
                        Name(String name1, String ignore) {
                            this.name = name1;
                        }
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/new_class/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.new_class.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Option<String> greet = (Option<String>) method.invoke(null, 0);
        assertThat(greet.isEmpty()).isTrue();

        // invoke with negative
        greet = (Option<String>) method.invoke(null, -10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Alex");

        // invoke with positive
        greet = (Option<String>) method.invoke(null, 10);
        assertThat(greet.isEmpty()).isFalse();
        assertThat(greet.get()).isEqualTo("Sergei");
    }
}
