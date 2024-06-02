package dev.khbd.result4j.javac;

import static com.sun.tools.javac.code.Flags.UNATTRIBUTED;

import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.util.TreeScanner;
import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.MemberEnter;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

/**
 * @author Sergei Khadanovich
 */
class ReAttributer {

    private final Attr attr;
    private final Enter enter;
    private final MemberEnter memberEnter;

    ReAttributer(Context context) {
        this.attr = Attr.instance(context);
        this.enter = Enter.instance(context);
        this.memberEnter = MemberEnter.instance(context);
    }

    /**
     * Reattribute compilation unit.
     */
    void attribute(JCTree.JCCompilationUnit unit) {
//        unit.accept(new RemoveTypesScanner(), null);
        unit.getTypeDecls()
                .stream()
                .filter(declaration -> declaration instanceof JCTree.JCClassDecl)
                .map(declaration -> (JCTree.JCClassDecl) declaration)
                .forEach(this::attribute);
    }

    private void attribute(JCTree.JCClassDecl classDeclaration) {
        var env = enterClass(classDeclaration);
        classDeclaration.defs.stream()
                .filter(definition -> definition instanceof JCTree.JCMethodDecl)
                .map(definition -> (JCTree.JCMethodDecl) definition)
                .forEach(methodDecl -> enterMethod(methodDecl, env));
    }

    private Env<AttrContext> enterClass(JCTree.JCClassDecl classDeclaration) {
        var env = enter.getClassEnv(classDeclaration.sym);
        classDeclaration.sym.flags_field = classDeclaration.sym.flags_field | UNATTRIBUTED;
        attr.attrib(env);
        return env;
    }

    private void enterMethod(JCTree.JCMethodDecl methodDecl, Env<AttrContext> parent) {
        var env = memberEnter.getMethodEnv(methodDecl, parent);
        methodDecl.sym.flags_field = methodDecl.sym.flags_field | UNATTRIBUTED;
        attr.attrib(env);
    }

    static class RemoveTypesScanner extends TreeScanner<Void, Void> {

        @Override
        public Void visitVariable(VariableTree node, Void unused) {
            var decl = (JCTree.JCVariableDecl) node;

            if (decl.declaredUsingVar()) {
                decl.type = null;
                decl.vartype = null;
            }

            return super.visitVariable(node, unused);
        }

        @Override
        public Void visitMethodInvocation(MethodInvocationTree node, Void unused) {
            var invocation = (JCTree.JCMethodInvocation) node;
            invocation.type = null;
            invocation.meth.type = null;
            return super.visitMethodInvocation(node, unused);
        }
    }
}
