package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Result;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class BinaryTest extends AbstractPluginTest {

    @Test
    public void propagate_inBinaryBoth() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, Integer> getSum(boolean flag1, boolean flag2) {
                        var result = getInt(flag1).unwrap() + getInt(flag2).unwrap();
                        return Result.success(result);
                    }
                    
                    public static Result<String, Integer> getInt(boolean flag) {
                        return flag ? Result.success(10) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class, boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        sum = (Result<String, Integer>) method.invoke(null, true, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        sum = (Result<String, Integer>) method.invoke(null, false, true);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(20);
    }

    @Test
    public void propagate_inBinaryLeft() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, Integer> getSum(boolean flag) {
                        var result = getInt(flag).unwrap() + 1;
                        return Result.success(result);
                    }
                    
                    public static Result<String, Integer> getInt(boolean flag) {
                        return flag ? Result.success(10) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inBinaryRight() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, Integer> getSum(boolean flag) {
                        var result = 1 + getInt(flag).unwrap();
                        return Result.success(result);
                    }
                    
                    public static Result<String, Integer> getInt(boolean flag) {
                        return flag ? Result.success(10) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(11);
    }

    @Test
    public void propagate_inComplexBinary() throws Exception {
        String source = """
                package cases.in_binary;
                                
                import dev.khbd.result4j.core.Result;
                                
                public class Main {
                                
                    public static Result<String, Integer> getSum(boolean flag) {
                        var result = (getInt(flag).unwrap() + getInt(flag).unwrap()) + getInt(flag).unwrap();
                        return Result.success(result);
                    }
                    
                    public static Result<String, Integer> getInt(boolean flag) {
                        return flag ? Result.success(10) : Result.error("error");
                    }
                }
                """;

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/in_binary/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.in_binary.Main");
        Method method = clazz.getMethod("getSum", boolean.class);

        // call with false
        Result<String, Integer> sum = (Result<String, Integer>) method.invoke(null, false);
        assertThat(sum.isError()).isTrue();
        assertThat(sum.getError()).isEqualTo("error");

        // call with true
        sum = (Result<String, Integer>) method.invoke(null, true);
        assertThat(sum.isSuccess()).isTrue();
        assertThat(sum.get()).isEqualTo(30);
    }
}
