package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Statement process to replace unwrap call by propagating logic.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class UnwrapCallReplacerStatementProcessor implements StatementProcessor {

    private final UnwrapCallSearcher searcher;
    private final PropagateLogicBuilder propagateLogicBuilder;

    @Override
    public ProcessedStatement process(JCTree.JCStatement statement) {
        UnwrapCallLens lens = searcher.visit(statement, null);

        if (lens == null) {
            return new ProcessedStatement(false, statement);
        }

        PropagateLogic logic = propagateLogicBuilder.build(lens.receiver(), statement.pos);

        lens.replaceF().accept(logic.ident());

        return new ProcessedStatement(true,
                List.<JCTree.JCStatement>nil()
                        .appendList(logic.statements())
                        .append(statement)
        );
    }
}
