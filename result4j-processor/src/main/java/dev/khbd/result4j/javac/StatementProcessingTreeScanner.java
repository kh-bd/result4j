package dev.khbd.result4j.javac;

import com.sun.source.tree.BlockTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import dev.khbd.result4j.javac.StatementProcessor.ProcessedStatement;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Tree scanner to process statements in blocks.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class StatementProcessingTreeScanner extends TreeScanner<Boolean, Object> {

    private final List<StatementProcessor> processors;

    @Override
    public Boolean visitBlock(BlockTree node, Object o) {
        JCTree.JCBlock jcBlock = (JCTree.JCBlock) node;

        List<JCTree.JCStatement> newStatements = List.nil();
        boolean processed = false;

        for (JCTree.JCStatement statement : jcBlock.stats) {
            ProcessedStatement processedStatement = applyFirstProcessor(statement);
            newStatements = newStatements.appendList(processedStatement.statements());
            processed |= processedStatement.processed();
        }

        jcBlock.stats = newStatements;

        return reduce(processed, super.visitBlock(node, o));
    }

    private ProcessedStatement applyFirstProcessor(JCTree.JCStatement statement) {
        for (var processor : processors) {
            ProcessedStatement processed = processor.process(statement);
            if (processed.processed()) {
                return processed;
            }
        }
        return new ProcessedStatement(false, statement);
    }

    @Override
    public Boolean reduce(Boolean r1, Boolean r2) {
        return toBoolean(r1) || toBoolean(r2);
    }

    private static boolean toBoolean(Boolean value) {
        return Objects.nonNull(value) && value;
    }
}
