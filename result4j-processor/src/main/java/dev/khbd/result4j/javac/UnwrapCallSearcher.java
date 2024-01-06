package dev.khbd.result4j.javac;

import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;

import java.util.Objects;

/**
 * Unwrap lens searcher.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class UnwrapCallSearcher extends TreeScanner<UnwrapCallLens, Object> {

    private final Symbol type;

    @Override
    public UnwrapCallLens visitThrow(ThrowTree node, Object o) {
        JCTree.JCThrow jcThrow = (JCTree.JCThrow) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcThrow.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcThrow.expr = expr);
        }

        return scan(jcThrow.expr, o);
    }

    @Override
    public UnwrapCallLens visitVariable(VariableTree node, Object o) {
        JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcVariable.init);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcVariable.init = expr);
        }

        return scan(jcVariable.init, o);
    }

    @Override
    public UnwrapCallLens visitExpressionStatement(ExpressionStatementTree node, Object o) {
        JCTree.JCExpressionStatement jcExprStatement = (JCTree.JCExpressionStatement) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcExprStatement.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcExprStatement.expr = expr);
        }

        return scan(jcExprStatement.expr, o);
    }

    @Override
    public UnwrapCallLens visitEnhancedForLoop(EnhancedForLoopTree node, Object o) {
        JCTree.JCEnhancedForLoop jcLoop = (JCTree.JCEnhancedForLoop) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcLoop.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcLoop.expr = expr);
        }

        return scan(jcLoop.expr, o);
    }

    @Override
    public UnwrapCallLens visitForLoop(ForLoopTree node, Object o) {
        // stop deep scanning. for loop body is scanned during separate steps
        return null;
    }

    @Override
    public UnwrapCallLens visitWhileLoop(WhileLoopTree node, Object o) {
        // stop deep scanning. while loop body is scanned during separate steps
        return null;
    }

    @Override
    public UnwrapCallLens visitDoWhileLoop(DoWhileLoopTree node, Object o) {
        // stop deep scanning. do while loop body is scanned during separate steps
        return null;
    }

    @Override
    public UnwrapCallLens visitMethodInvocation(MethodInvocationTree node, Object o) {
        JCTree.JCMethodInvocation jcCall = (JCTree.JCMethodInvocation) node;

        // jcCall.meth cannot be the unwrap method call.
        // either field access or ident.
        // So, do not need to search lens in jcCall.meth directly

        UnwrapCallLens methLens = scan(jcCall.meth, o);
        if (Objects.nonNull(methLens)) {
            return methLens;
        }

        List<JCTree.JCExpression> args = jcCall.args;
        for (JCTree.JCExpression arg : args) {
            JCTree.JCExpression argReceiver = getUnwrapCallReceiver(arg);
            if (Objects.nonNull(argReceiver)) {
                return new UnwrapCallLens(argReceiver, expr -> jcCall.args = replace(args, arg, expr));
            }
            UnwrapCallLens argLens = scan(arg, o);
            if (Objects.nonNull(argLens)) {
                return argLens;
            }
        }

        return null;
    }

    @Override
    public UnwrapCallLens visitMemberSelect(MemberSelectTree node, Object o) {
        JCTree.JCFieldAccess jcField = (JCTree.JCFieldAccess) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcField.selected);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcField.selected = expr);
        }

        return scan(jcField.selected, o);
    }

    @Override
    public UnwrapCallLens visitReturn(ReturnTree node, Object o) {
        // do not need special support because
        // node.getExpression cannot be the unwrap method call.
        // So, search method call deeply
        return super.visitReturn(node, o);
    }

    @Override
    public UnwrapCallLens visitConditionalExpression(ConditionalExpressionTree node, Object o) {
        // do not analise conditional expression parts
        return null;
    }

    @Override
    public UnwrapCallLens visitIf(IfTree node, Object o) {
        // do not analise if condition
        return null;
    }

    @Override
    public UnwrapCallLens reduce(UnwrapCallLens r1, UnwrapCallLens r2) {
        return Objects.nonNull(r1) ? r1 : r2;
    }

    private static List<JCTree.JCExpression> replace(List<JCTree.JCExpression> list,
                                                     JCTree.JCExpression original,
                                                     JCTree.JCExpression replacement) {
        return list.map(expr -> expr == original ? replacement : expr);
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
