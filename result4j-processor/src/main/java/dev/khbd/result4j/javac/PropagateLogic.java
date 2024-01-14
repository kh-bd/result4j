package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Propagate logic.
 */
@Getter
@RequiredArgsConstructor
class PropagateLogic {

    private final List<JCTree.JCStatement> statements;
    private final JCTree.JCExpression ident;
}
