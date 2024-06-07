package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.function.Consumer;

/**
 * Unwrap call lens.
 *
 * @author Sergei Khadanovich
 */
@Getter
@RequiredArgsConstructor
class UnwrapCallLens {

    private final JCTree.JCExpression receiver;
    private final Consumer<JCTree.JCIdent> replaceF;
}
