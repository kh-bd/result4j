package dev.khbd.result4j.javac.either;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.core.Either;
import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import java.lang.reflect.Method;

/**
 * @author Sergei_Khadanovich
 */
public class LocalVariableDeclarationTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapOnLocalVarInitExpression_propagate() throws Exception {
        String source =
                "package cases.local_variable_declaration;\n" +
                "\n" +
                "import dev.khbd.result4j.core.Either;\n" +
                "\n" +
                "public class Main {\n" +
                "\n" +
                "    public static Either<Exception, String> greet(int index) {\n" +
                "        var name = name(index).unwrap();\n" +
                "        return Either.right(name.toUpperCase());\n" +
                "    }\n" +
                "\n" +
                "    private static Either<Exception, String> name(int index) {\n" +
                "        if (index == 0) {\n" +
                "            return Either.left(new RuntimeException(\"ERROR\"));\n" +
                "        }\n" +
                "        return Either.right(index < 0 ? \"Alex\" : \"Sergei\");\n" +
                "    }\n" +
                "}\n";

        CompilationResult result = compiler.compile(new PluginOptions(true), "cases/local_variable_declaration/Main.java", source);

        assertThat(result.isSuccess()).isTrue();

        ClassLoader classLoader = result.classLoader();
        Class<?> clazz = classLoader.loadClass("cases.local_variable_declaration.Main");
        Method method = clazz.getMethod("greet", int.class);

        // invoke with 0
        Either<Exception, String> greet = (Either<Exception, String>) method.invoke(null, 0);
        assertThat(greet.isLeft()).isTrue();
        assertThat(greet.getLeft()).isInstanceOf(RuntimeException.class)
                .hasMessage("ERROR");

        // invoke with negative
        greet = (Either<Exception, String>) method.invoke(null, -10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("ALEX");

        // invoke with positive
        greet = (Either<Exception, String>) method.invoke(null, 10);
        assertThat(greet.isRight()).isTrue();
        assertThat(greet.getRight()).isEqualTo("SERGEI");
    }
}
