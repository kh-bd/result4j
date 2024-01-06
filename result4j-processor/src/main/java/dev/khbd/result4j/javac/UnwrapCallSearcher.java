package dev.khbd.result4j.javac;

import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import lombok.RequiredArgsConstructor;

/**
 * Unwrap lens searcher.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class UnwrapCallSearcher extends TreeScanner<UnwrapCallLens, Object> {

    private final Symbol type;

    @Override
    public UnwrapCallLens visitVariable(VariableTree node, Object o) {
        JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcVariable.init);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcVariable.init = expr);
        }

        return scan(node.getInitializer(), o);
    }

    @Override
    public UnwrapCallLens visitExpressionStatement(ExpressionStatementTree node, Object o) {
        JCTree.JCExpressionStatement jcExprStatement = (JCTree.JCExpressionStatement) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcExprStatement.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcExprStatement.expr = expr);
        }

        return scan(node.getExpression(), o);
    }

    @Override
    public UnwrapCallLens visitEnhancedForLoop(EnhancedForLoopTree node, Object o) {
        JCTree.JCEnhancedForLoop jcLoop = (JCTree.JCEnhancedForLoop) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcLoop.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcLoop.expr = expr);
        }

        return scan(node.getExpression(), o);
    }

    private JCTree.JCExpression getUnwrapCallReceiver(JCTree.JCExpression expression) {
        if (expression == null) {
            return null;
        }

        if (expression.getKind() != Tree.Kind.METHOD_INVOCATION) {
            return null;
        }

        JCTree.JCMethodInvocation jcInvoke = (JCTree.JCMethodInvocation) expression;
        if (jcInvoke.meth.getKind() != Tree.Kind.MEMBER_SELECT) {
            return null;
        }

        JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) jcInvoke.meth;
        if (!fieldAccess.name.contentEquals("unwrap")) {
            return null;
        }

        if (fieldAccess.selected.type == null) {
            return null;
        }

        if (fieldAccess.selected.type.tsym.equals(type)) {
            return fieldAccess.selected;
        }

        return null;
    }
}
