package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;

/**
 * Propagate logic builder.
 *
 * @author Sergei Khadanovich
 */
interface PropagateLogicBuilder {

    /**
     * Build propagate logic from receiver expression and position.
     */
    PropagateLogic build(JCTree.JCExpression receiver, int position);
}
