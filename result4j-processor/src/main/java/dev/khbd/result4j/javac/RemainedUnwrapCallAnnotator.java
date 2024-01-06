package dev.khbd.result4j.javac;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.List;
import lombok.RequiredArgsConstructor;

/**
 * Special tree scanner to find unwrap method calls and mark them as erroneous.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class RemainedUnwrapCallAnnotator extends TreeScanner<Object, Object> {

    private final Logger logger;
    private final List<Symbol> types;

    @Override
    public Object visitMethodInvocation(MethodInvocationTree node, Object o) {
        super.visitMethodInvocation(node, o);

        JCTree.JCMethodInvocation jcMethodCall = (JCTree.JCMethodInvocation) node;

        if (unwrapCall(jcMethodCall)) {
            logger.logError(jcMethodCall, "unwrap.call.at.unsupported.position");
        }

        return null;
    }

    private boolean unwrapCall(JCTree.JCMethodInvocation jcMethodCall) {
        if (jcMethodCall.meth.getKind() != Tree.Kind.MEMBER_SELECT) {
            return false;
        }

        JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) jcMethodCall.meth;
        if (!fieldAccess.name.contentEquals("unwrap")) {
            return false;
        }

        return oneOfSupportedType(fieldAccess.selected);
    }

    private boolean oneOfSupportedType(JCTree.JCExpression expression) {
        if (expression.type == null) {
            return false;
        }
        for (Symbol type : types) {
            if (expression.type.tsym.equals(type)) {
                return true;
            }
        }
        return false;
    }
}
