package dev.khbd.result4j.javac;

import static com.sun.tools.javac.code.Flags.UNATTRIBUTED;

import com.sun.tools.javac.comp.Attr;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;

/**
 * @author Sergei Khadanovich
 */
class ReAttributer {

    private final Attr attr;
    private final Enter enter;

    ReAttributer(Context context) {
        this.attr = Attr.instance(context);
        this.enter = Enter.instance(context);
    }

    /**
     * Reattribute compilation unit.
     */
    void attribute(JCTree.JCCompilationUnit unit) {
        unit.getTypeDecls()
                .stream()
                .filter(declaration -> declaration instanceof JCTree.JCClassDecl)
                .map(declaration -> (JCTree.JCClassDecl) declaration)
                .forEach(this::enterClass);
    }

    private void enterClass(JCTree.JCClassDecl classDeclaration) {
        var env = enter.getClassEnv(classDeclaration.sym);
        classDeclaration.sym.flags_field = classDeclaration.sym.flags_field | UNATTRIBUTED;
        attr.attrib(env);

        // sout();
    }
}
