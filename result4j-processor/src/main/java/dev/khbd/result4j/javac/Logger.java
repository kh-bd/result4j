package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.DiagnosticSource;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.Log;
import lombok.RequiredArgsConstructor;

import javax.tools.JavaFileObject;

/**
 * Logger instance.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class Logger {

    private final Log logger;
    private final JCDiagnostic.Factory diagnosticFactory;
    private final JavaFileObject javaFile;

    /**
     * Log error message.
     *
     * @param source tree element
     */
    void logError(JCTree source, String key, Object... args) {
        JCDiagnostic error = diagnosticFactory.error(JCDiagnostic.DiagnosticFlag.MANDATORY,
                new DiagnosticSource(javaFile, logger),
                new JCDiagnostic.SimpleDiagnosticPosition(source.pos),
                key, args
        );
        logger.report(error);
    }
}
