package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;

import java.util.function.Consumer;

/**
 * Unwrap call lens.
 *
 * @param receiver expression on which unwrap function was called
 * @param replaceF function to consume replacement
 * @author Sergei Khadanovich
 */
record UnwrapCallLens(JCTree.JCExpression receiver, Consumer<JCTree.JCExpression> replaceF) {
}
