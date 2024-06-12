package dev.khbd.result4j.javac.option;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Option;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;
import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class BinaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inBinaryOrBoth() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Boolean> get(boolean flag1, boolean flag2) {
                        var result = getByFlag(flag1).unwrap() || getByFlag(flag2).unwrap();
                        return Option.some(result);
                    }
                
                    public static Option<Boolean> getByFlag(boolean flag) {
                        return Option.some(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryOrRight() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Boolean> get(boolean flag1, boolean flag2) {
                        var result = flag1 || getByFlag(flag2).unwrap();
                        return Option.some(result);
                    }
                
                    public static Option<Boolean> getByFlag(boolean flag) {
                        return Option.some(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryOrLeft() throws Exception {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Boolean> get(boolean flag1, boolean flag2) {
                        var result = getByFlag(flag1).unwrap() || flag2;
                        return Option.some(result);
                    }
                
                    public static Option<Boolean> getByFlag(boolean flag) {
                        return Option.some(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("get", boolean.class, boolean.class);

        // call with false
        Option<Boolean> unwrapped = (Option<Boolean>) method.invoke(null, false, false);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        unwrapped = (Option<Boolean>) method.invoke(null, true, false);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);

        unwrapped = (Option<Boolean>) method.invoke(null, false, true);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);

        // call with true
        unwrapped = (Option<Boolean>) method.invoke(null, true, true);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);
    }

    @Test
    public void propagate_inBinaryAndBoth() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Boolean> get(boolean flag1, boolean flag2) {
                        var result = getByFlag(flag1).unwrap() && getByFlag(flag2).unwrap();
                        return Option.some(result);
                    }
                
                    public static Option<Boolean> getByFlag(boolean flag) {
                        return Option.some(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryAndRight() {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Boolean> get(boolean flag1, boolean flag2) {
                        var result = flag1 && getByFlag(flag2).unwrap();
                        return Option.some(result);
                    }
                
                    public static Option<Boolean> getByFlag(boolean flag) {
                        return Option.some(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }

    @Test
    public void propagate_inBinaryAndLeft() throws Exception {
        String source = """
                package cases.in_binary;
                
                import dev.khbd.result4j.core.Option;
                
                public class Main {
                
                    public static Option<Boolean> get(boolean flag1, boolean flag2) {
                        var result = getByFlag(flag1).unwrap() && flag2;
                        return Option.some(result);
                    }
                
                    public static Option<Boolean> getByFlag(boolean flag) {
                        return Option.some(flag);
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("get", boolean.class, boolean.class);

        // call with false
        Option<Boolean> unwrapped = (Option<Boolean>) method.invoke(null, false, false);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        unwrapped = (Option<Boolean>) method.invoke(null, true, false);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        unwrapped = (Option<Boolean>) method.invoke(null, false, true);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(false);

        // call with true
        unwrapped = (Option<Boolean>) method.invoke(null, true, true);
        assertThat(unwrapped.isEmpty()).isFalse();
        assertThat(unwrapped.get()).isEqualTo(true);
    }

    @Test
    public void propagate_inBinaryBoth() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag1, boolean flag2) {
                        var result = getInt(flag1).unwrap() + getInt(flag2).unwrap();
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class, boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false, false);
        assertThat(sum.isEmpty()).isTrue();

        sum = (Option<Integer>) method.invoke(null, true, false);
        assertThat(sum.isEmpty()).isTrue();

        sum = (Option<Integer>) method.invoke(null, false, true);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(20);
    }

    @Test
    public void propagate_inBinaryLeft() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag) {
                        var result = getInt(flag).unwrap() + 1;
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inBinaryRight() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag) {
                        var result = 1 + getInt(flag).unwrap();
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inComplexBinary() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Option;
                                
                public class Main {
                                
                    public static Option<Integer> getSum(boolean flag) {
                        var result = (getInt(flag).unwrap() + getInt(flag).unwrap()) + getInt(flag).unwrap();
                        return Option.some(result);
                    }
                    
                    public static Option<Integer> getInt(boolean flag) {
                        return flag ? Option.some(10) : Option.none();
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Option<Integer> sum = (Option<Integer>) method.invoke(null, false);
        assertThat(sum.isEmpty()).isTrue();

        // call with true
        sum = (Option<Integer>) method.invoke(null, true);
        assertThat(sum.isEmpty()).isFalse();
        assertThat(sum.get()).isEqualTo(30);
    }
}
