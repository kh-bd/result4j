package dev.khbd.result4j.javac.result;

import static org.assertj.core.api.Assertions.assertThat;

import dev.khbd.result4j.javac.AbstractPluginTest;
import org.testng.annotations.Test;

import javax.tools.Diagnostic;

/**
 * @author Sergei_Khadanovich
 */
public class StringTemplateTest extends AbstractPluginTest {

    @Test
    public void propagate_unwrapCallInStringTemplate_failToCompile() {
        CompilationResult result = compiler.compile(new PluginOptions(true), "/cases/result/string_template/Main.java");

        assertThat(result.isFail()).isTrue();
        assertThat(result.getErrors()).extracting(Diagnostic::toString)
                .anyMatch(msg -> msg.contains(" Unsupported position for unwrap method call"));
    }
}
