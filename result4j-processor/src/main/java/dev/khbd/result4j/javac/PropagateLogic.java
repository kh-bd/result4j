package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.Value;

/**
 * Propagate logic.
 */
@Value
class PropagateLogic {

    List<JCTree.JCStatement> statements;
    JCTree.JCIdent ident;
}
