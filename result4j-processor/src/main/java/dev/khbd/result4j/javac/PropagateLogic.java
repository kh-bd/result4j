package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;

/**
 * Propagate logic.
 *
 * @param statements statements with propagation logic
 * @param ident      expression which can be inserted in place of original receiver
 */
record PropagateLogic(List<JCTree.JCStatement> statements, JCTree.JCIdent ident) {
}
