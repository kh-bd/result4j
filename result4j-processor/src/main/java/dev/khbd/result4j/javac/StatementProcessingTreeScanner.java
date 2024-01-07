package dev.khbd.result4j.javac;

import com.sun.source.tree.BlockTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import dev.khbd.result4j.javac.StatementProcessor.ProcessedStatement;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Tree scanner to process statements in blocks.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class StatementProcessingTreeScanner extends TreeScanner<Boolean, Object> {

    private final List<StatementProcessor> processors;
    private final TreeMaker treeMaker;

    StatementProcessingTreeScanner(List<StatementProcessor> processors,
                                   Context context) {
        this.processors = processors;
        this.treeMaker = TreeMaker.instance(context);
    }

    @Override
    public Boolean visitBlock(BlockTree node, Object o) {
        JCTree.JCBlock jcBlock = (JCTree.JCBlock) node;

        ProcessedStatement processed = processSeveralStatements(jcBlock.stats);

        if (processed.processed()) {
            jcBlock.stats = processed.statements();
        }

        return reduce(processed.processed(), scan(jcBlock.stats, o));
    }

    @Override
    public Boolean visitForLoop(ForLoopTree node, Object o) {
        JCTree.JCForLoop jcLoop = (JCTree.JCForLoop) node;
        return processOneStatementBlock(jcLoop.body, st -> jcLoop.body = st, o);
    }

    @Override
    public Boolean visitEnhancedForLoop(EnhancedForLoopTree node, Object o) {
        JCTree.JCEnhancedForLoop jcLoop = (JCTree.JCEnhancedForLoop) node;
        return processOneStatementBlock(jcLoop.body, st -> jcLoop.body = st, 0);
    }

    @Override
    public Boolean visitWhileLoop(WhileLoopTree node, Object o) {
        JCTree.JCWhileLoop jcLoop = (JCTree.JCWhileLoop) node;
        return processOneStatementBlock(jcLoop.body, st -> jcLoop.body = st, o);
    }

    @Override
    public Boolean visitDoWhileLoop(DoWhileLoopTree node, Object o) {
        JCTree.JCDoWhileLoop jcLoop = (JCTree.JCDoWhileLoop) node;
        return processOneStatementBlock(jcLoop.body, st -> jcLoop.body = st, o);
    }

    @Override
    public Boolean visitIf(IfTree node, Object o) {
        JCTree.JCIf jcIf = (JCTree.JCIf) node;

        return reduce(
                processOneStatementBlock(jcIf.thenpart, st -> jcIf.thenpart = st, o),
                processOneStatementBlock(jcIf.elsepart, st -> jcIf.elsepart = st, o)
        );
    }

    @Override
    public Boolean visitSwitch(SwitchTree node, Object o) {
        JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) node;
        return scan(jcSwitch.cases, o);
    }

    @Override
    public Boolean visitCase(CaseTree node, Object o) {
        JCTree.JCCase jcCase = (JCTree.JCCase) node;

        ProcessedStatement processed = processSeveralStatements(jcCase.stats);

        if (processed.processed()) {
            jcCase.stats = processed.statements();
        }

        return reduce(processed.processed(), scan(jcCase.stats, o));
    }

    private Boolean processOneStatementBlock(JCTree.JCStatement statement,
                                             Consumer<JCTree.JCStatement> changedStatementApplier,
                                             Object o) {
        if (statement == null) {
            return Boolean.FALSE;
        }

        if (statement.getKind() == Tree.Kind.BLOCK) {
            return scan(statement, o);
        }

        ProcessedStatement processed = applyFirstProcessor(statement);
        if (!processed.processed()) {
            return Boolean.FALSE;
        }

        JCTree.JCStatement changed = processed.statements().size() == 1
                ? processed.statements().head
                : treeMaker.at(statement.pos).Block(0, processed.statements());

        changedStatementApplier.accept(changed);

        return Boolean.TRUE;
    }

    private ProcessedStatement processSeveralStatements(List<JCTree.JCStatement> statements) {
        List<JCTree.JCStatement> newStatements = List.nil();
        boolean processed = false;

        for (JCTree.JCStatement statement : statements) {
            ProcessedStatement processedStatement = applyFirstProcessor(statement);
            newStatements = newStatements.appendList(processedStatement.statements());
            processed |= processedStatement.processed();
        }

        return new ProcessedStatement(processed, newStatements);
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
