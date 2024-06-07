package dev.khbd.result4j.javac;

import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.SimpleTreeVisitor;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;

import java.util.Objects;
import java.util.function.Consumer;

/**
 * Unwrap lens searcher.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class UnwrapCallSearcher extends SimpleTreeVisitor<UnwrapCallLens, Object> {

    private final Symbol type;

    @Override
    public UnwrapCallLens visitTry(TryTree node, Object o) {
        JCTree.JCTry jcTry = (JCTree.JCTry) node;

        if (jcTry.resources != null) {
            for (int i = 0; i < jcTry.resources.size(); i++) {
                JCTree jcTree = jcTry.getResources().get(i);
                if (jcTree.getKind() != Tree.Kind.VARIABLE) {
                    continue;
                }

                JCTree.JCVariableDecl jcVar = (JCTree.JCVariableDecl) jcTree;
                JCTree.JCExpression receiver = getUnwrapCallReceiver(jcVar.init);
                if (receiver != null) {
                    int current = i;
                    return new UnwrapCallLens(receiver, expr -> {
                        jcVar.init = expr;
                        jcVar.sym = null;

                        // replace current variable name with new generated name
                        // for each remaining var declarations
                        for (int j = current + 1; j < jcTry.resources.size(); j++) {
                            JCTree nextTree = jcTry.getResources().get(j);
                            nextTree.accept(new TreeScanner<Void, Void>() {
                                @Override
                                public Void visitIdentifier(IdentifierTree node, Void unused) {
                                    JCTree.JCIdent ident = (JCTree.JCIdent) node;
                                    if (ident.name.equals(jcVar.name)) {
                                        ident.name = expr.name;
                                    }
                                    return null;
                                }
                            }, null);
                        }
                    });
                }
            }
        }

        return null;
    }

    @Override
    public UnwrapCallLens visitBinary(BinaryTree node, Object o) {
        JCTree.JCBinary jcBinary = (JCTree.JCBinary) node;

        JCTree.JCExpression leftReceiver = getUnwrapCallReceiver(jcBinary.lhs);
        if (leftReceiver != null) {
            return new UnwrapCallLens(leftReceiver, expr -> jcBinary.lhs = expr);
        }

        JCTree.JCExpression rightReceiver = getUnwrapCallReceiver(jcBinary.rhs);
        if (rightReceiver != null) {
            return new UnwrapCallLens(rightReceiver, expr -> jcBinary.rhs = expr);
        }

        return reduce(visit(jcBinary.lhs, o), visit(jcBinary.rhs, o));
    }

    @Override
    public UnwrapCallLens visitCompoundAssignment(CompoundAssignmentTree node, Object o) {
        JCTree.JCAssignOp jcAssign = (JCTree.JCAssignOp) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcAssign.rhs);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcAssign.rhs = expr);
        }

        return visit(jcAssign.rhs, o);
    }

    @Override
    public UnwrapCallLens visitAssignment(AssignmentTree node, Object o) {
        JCTree.JCAssign jcAssign = (JCTree.JCAssign) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcAssign.rhs);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcAssign.rhs = expr);
        }

        return visit(jcAssign.rhs, o);
    }

    @Override
    public UnwrapCallLens visitUnary(UnaryTree node, Object o) {
        JCTree.JCUnary jcUnary = (JCTree.JCUnary) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcUnary.arg);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcUnary.arg = expr);
        }

        return visit(jcUnary.arg, o);
    }

    @Override
    public UnwrapCallLens visitSwitch(SwitchTree node, Object o) {
        JCTree.JCSwitch jcSwitch = (JCTree.JCSwitch) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcSwitch.selector);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcSwitch.selector = expr);
        }

        return visit(jcSwitch.selector, o);
    }

    @Override
    public UnwrapCallLens visitParenthesized(ParenthesizedTree node, Object o) {
        JCTree.JCParens jcParens = (JCTree.JCParens) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcParens.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcParens.expr = expr);
        }

        return visit(jcParens.expr, o);
    }

    @Override
    public UnwrapCallLens visitSynchronized(SynchronizedTree node, Object o) {
        JCTree.JCSynchronized jcSync = (JCTree.JCSynchronized) node;

        // sync lock is jcParens, so do not try to analise it.
        // go one step deeper

        return visit(jcSync.lock, o);
    }

    @Override
    public UnwrapCallLens visitThrow(ThrowTree node, Object o) {
        JCTree.JCThrow jcThrow = (JCTree.JCThrow) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcThrow.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcThrow.expr = expr);
        }

        return visit(jcThrow.expr, o);
    }

    @Override
    public UnwrapCallLens visitVariable(VariableTree node, Object o) {
        JCTree.JCVariableDecl jcVariable = (JCTree.JCVariableDecl) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcVariable.init);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcVariable.init = expr);
        }

        return visit(jcVariable.init, o);
    }

    @Override
    public UnwrapCallLens visitExpressionStatement(ExpressionStatementTree node, Object o) {
        JCTree.JCExpressionStatement jcExprStatement = (JCTree.JCExpressionStatement) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcExprStatement.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcExprStatement.expr = expr);
        }

        return visit(jcExprStatement.expr, o);
    }

    @Override
    public UnwrapCallLens visitEnhancedForLoop(EnhancedForLoopTree node, Object o) {
        JCTree.JCEnhancedForLoop jcLoop = (JCTree.JCEnhancedForLoop) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcLoop.expr);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcLoop.expr = expr);
        }

        return visit(jcLoop.expr, o);
    }

    @Override
    public UnwrapCallLens visitMethodInvocation(MethodInvocationTree node, Object o) {
        JCTree.JCMethodInvocation jcCall = (JCTree.JCMethodInvocation) node;

        // jcCall.meth cannot be the unwrap method call.
        // either field access or ident.
        // So, do not need to search lens in jcCall.meth directly

        UnwrapCallLens methLens = visit(jcCall.meth, o);
        if (Objects.nonNull(methLens)) {
            return methLens;
        }

        return visitExpressions(jcCall.args, replacement -> jcCall.args = replacement, o);
    }

    @Override
    public UnwrapCallLens visitArrayAccess(ArrayAccessTree node, Object o) {
        JCTree.JCArrayAccess jcAccess = (JCTree.JCArrayAccess) node;

        JCTree.JCExpression instanceReceiver = getUnwrapCallReceiver(jcAccess.indexed);
        if (instanceReceiver != null) {
            return new UnwrapCallLens(instanceReceiver, expr -> jcAccess.indexed = expr);
        }

        JCTree.JCExpression indexReceiver = getUnwrapCallReceiver(jcAccess.index);
        if (indexReceiver != null) {
            return new UnwrapCallLens(indexReceiver, expr -> jcAccess.index = expr);
        }

        return reduce(visit(jcAccess.indexed, o), visit(jcAccess.index, o));
    }

    @Override
    public UnwrapCallLens visitNewArray(NewArrayTree node, Object o) {
        JCTree.JCNewArray jcNew = (JCTree.JCNewArray) node;

        UnwrapCallLens lens = visitExpressions(jcNew.dims, replacement -> jcNew.dims = replacement, o);
        if (Objects.nonNull(lens)) {
            return lens;
        }

        return visitExpressions(jcNew.elems, replacement -> jcNew.elems = replacement, o);
    }

    @Override
    public UnwrapCallLens visitNewClass(NewClassTree node, Object o) {
        JCTree.JCNewClass jcNew = (JCTree.JCNewClass) node;
        return visitExpressions(jcNew.args, replacement -> jcNew.args = replacement, o);
    }

    @Override
    public UnwrapCallLens visitMemberSelect(MemberSelectTree node, Object o) {
        JCTree.JCFieldAccess jcField = (JCTree.JCFieldAccess) node;

        JCTree.JCExpression receiver = getUnwrapCallReceiver(jcField.selected);
        if (receiver != null) {
            return new UnwrapCallLens(receiver, expr -> jcField.selected = expr);
        }

        return visit(jcField.selected, o);
    }

    @Override
    public UnwrapCallLens visitReturn(ReturnTree node, Object o) {
        // do not need special support because
        // node.getExpression cannot be the unwrap method call.
        // So, search method call deeply
        return visit(node.getExpression(), o);
    }

    private UnwrapCallLens reduce(UnwrapCallLens r1, UnwrapCallLens r2) {
        return Objects.nonNull(r1) ? r1 : r2;
    }

    private UnwrapCallLens visitExpressions(List<JCTree.JCExpression> expressions,
                                            Consumer<List<JCTree.JCExpression>> replaceF,
                                            Object o) {
        if (Objects.isNull(expressions)) {
            return null;
        }
        for (JCTree.JCExpression expression : expressions) {
            JCTree.JCExpression argReceiver = getUnwrapCallReceiver(expression);
            if (Objects.nonNull(argReceiver)) {
                return new UnwrapCallLens(argReceiver, expr -> replaceF.accept(replace(expressions, expression, expr)));
            }
            UnwrapCallLens argLens = visit(expression, o);
            if (Objects.nonNull(argLens)) {
                return argLens;
            }
        }
        return null;
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
