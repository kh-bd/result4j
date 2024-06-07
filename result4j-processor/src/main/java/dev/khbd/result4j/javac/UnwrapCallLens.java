package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.Value;

import java.util.function.Consumer;

/**
 * Unwrap call lens.
 *
 * @author Sergei Khadanovich
 */
@Value
class UnwrapCallLens {

    JCTree.JCExpression receiver;
    Consumer<JCTree.JCIdent> replaceF;
}
