package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

/**
 * Statement processor.
 *
 * @author Sergei Khadanovich
 */
interface StatementProcessor {

    /**
     * Process statement.
     *
     * @param statement original statement
     */
    ProcessedStatement process(JCTree.JCStatement statement);

    /**
     * Processed statement.
     */
    record ProcessedStatement(boolean processed, List<JCTree.JCStatement> statements) {

        ProcessedStatement(boolean processed, JCTree.JCStatement... statements) {
            this(processed, List.from(statements));
        }

        ProcessedStatement(boolean processed, JCTree.JCStatement statement) {
            this(processed, List.of(statement));
        }
    }
}

