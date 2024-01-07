package dev.khbd.result4j.javac;

import com.sun.source.tree.AnnotatedTypeTree;
import com.sun.source.tree.AnnotationTree;
import com.sun.source.tree.AnyPatternTree;
import com.sun.source.tree.ArrayAccessTree;
import com.sun.source.tree.ArrayTypeTree;
import com.sun.source.tree.AssertTree;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.BinaryTree;
import com.sun.source.tree.BindingPatternTree;
import com.sun.source.tree.BlockTree;
import com.sun.source.tree.BreakTree;
import com.sun.source.tree.CaseTree;
import com.sun.source.tree.CatchTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.CompoundAssignmentTree;
import com.sun.source.tree.ConditionalExpressionTree;
import com.sun.source.tree.ConstantCaseLabelTree;
import com.sun.source.tree.ContinueTree;
import com.sun.source.tree.DeconstructionPatternTree;
import com.sun.source.tree.DefaultCaseLabelTree;
import com.sun.source.tree.DoWhileLoopTree;
import com.sun.source.tree.EmptyStatementTree;
import com.sun.source.tree.EnhancedForLoopTree;
import com.sun.source.tree.ErroneousTree;
import com.sun.source.tree.ExportsTree;
import com.sun.source.tree.ExpressionStatementTree;
import com.sun.source.tree.ForLoopTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.IfTree;
import com.sun.source.tree.ImportTree;
import com.sun.source.tree.InstanceOfTree;
import com.sun.source.tree.IntersectionTypeTree;
import com.sun.source.tree.LabeledStatementTree;
import com.sun.source.tree.LambdaExpressionTree;
import com.sun.source.tree.LiteralTree;
import com.sun.source.tree.MemberReferenceTree;
import com.sun.source.tree.MemberSelectTree;
import com.sun.source.tree.MethodInvocationTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.ModifiersTree;
import com.sun.source.tree.ModuleTree;
import com.sun.source.tree.NewArrayTree;
import com.sun.source.tree.NewClassTree;
import com.sun.source.tree.OpensTree;
import com.sun.source.tree.PackageTree;
import com.sun.source.tree.ParameterizedTypeTree;
import com.sun.source.tree.ParenthesizedTree;
import com.sun.source.tree.PatternCaseLabelTree;
import com.sun.source.tree.PrimitiveTypeTree;
import com.sun.source.tree.ProvidesTree;
import com.sun.source.tree.RequiresTree;
import com.sun.source.tree.ReturnTree;
import com.sun.source.tree.StringTemplateTree;
import com.sun.source.tree.SwitchExpressionTree;
import com.sun.source.tree.SwitchTree;
import com.sun.source.tree.SynchronizedTree;
import com.sun.source.tree.ThrowTree;
import com.sun.source.tree.Tree;
import com.sun.source.tree.TreeVisitor;
import com.sun.source.tree.TryTree;
import com.sun.source.tree.TypeCastTree;
import com.sun.source.tree.TypeParameterTree;
import com.sun.source.tree.UnaryTree;
import com.sun.source.tree.UnionTypeTree;
import com.sun.source.tree.UsesTree;
import com.sun.source.tree.VariableTree;
import com.sun.source.tree.WhileLoopTree;
import com.sun.source.tree.WildcardTree;
import com.sun.source.tree.YieldTree;

/**
 * Empty tree visitor.
 *
 * @param <P> context type
 * @param <R> result type
 * @author Sergei Khadanovich
 */
interface EmptyTreeVisitor<R, P> extends TreeVisitor<R, P> {

    /**
     * Reduce two results into single one.
     */
    default R reduce(R r1, R r2) {
        return r1;
    }

    /**
     * Visit tree node.
     */
    default R visit(Tree tree, P p) {
        return tree != null ? tree.accept(this, p) : null;
    }

    /**
     * Visit several nodes.
     */
    default R visit(Iterable<? extends Tree> nodes, P p) {
        R r = null;
        if (nodes != null) {
            boolean first = true;
            for (Tree node : nodes) {
                r = (first ? visit(node, p) : visitAndReduce(node, p, r));
                first = false;
            }
        }
        return r;
    }

    default R visitAndReduce(Tree node, P p, R r) {
        return reduce(visit(node, p), r);
    }

    default R visitAndReduce(Iterable<? extends Tree> nodes, P p, R r) {
        return reduce(visit(nodes, p), r);
    }

    @Override
    default R visitAnnotatedType(AnnotatedTypeTree node, P p) {
        return null;
    }

    @Override
    default R visitAnnotation(AnnotationTree node, P p) {
        return null;
    }

    @Override
    default R visitMethodInvocation(MethodInvocationTree node, P p) {
        return null;
    }

    @Override
    default R visitAssert(AssertTree node, P p) {
        return null;
    }

    @Override
    default R visitAssignment(AssignmentTree node, P p) {
        return null;
    }

    @Override
    default R visitCompoundAssignment(CompoundAssignmentTree node, P p) {
        return null;
    }

    @Override
    default R visitBinary(BinaryTree node, P p) {
        return null;
    }

    @Override
    default R visitBlock(BlockTree node, P p) {
        return null;
    }

    @Override
    default R visitBreak(BreakTree node, P p) {
        return null;
    }

    @Override
    default R visitCase(CaseTree node, P p) {
        return null;
    }

    @Override
    default R visitCatch(CatchTree node, P p) {
        return null;
    }

    @Override
    default R visitClass(ClassTree node, P p) {
        return null;
    }

    @Override
    default R visitConditionalExpression(ConditionalExpressionTree node, P p) {
        return null;
    }

    @Override
    default R visitContinue(ContinueTree node, P p) {
        return null;
    }

    @Override
    default R visitDoWhileLoop(DoWhileLoopTree node, P p) {
        return null;
    }

    @Override
    default R visitErroneous(ErroneousTree node, P p) {
        return null;
    }

    @Override
    default R visitExpressionStatement(ExpressionStatementTree node, P p) {
        return null;
    }

    @Override
    default R visitEnhancedForLoop(EnhancedForLoopTree node, P p) {
        return null;
    }

    @Override
    default R visitForLoop(ForLoopTree node, P p) {
        return null;
    }

    @Override
    default R visitIdentifier(IdentifierTree node, P p) {
        return null;
    }

    @Override
    default R visitIf(IfTree node, P p) {
        return null;
    }

    @Override
    default R visitImport(ImportTree node, P p) {
        return null;
    }

    @Override
    default R visitArrayAccess(ArrayAccessTree node, P p) {
        return null;
    }

    @Override
    default R visitLabeledStatement(LabeledStatementTree node, P p) {
        return null;
    }

    @Override
    default R visitLiteral(LiteralTree node, P p) {
        return null;
    }

    @Override
    default R visitStringTemplate(StringTemplateTree node, P p) {
        return null;
    }

    @Override
    default R visitAnyPattern(AnyPatternTree node, P p) {
        return null;
    }

    @Override
    default R visitBindingPattern(BindingPatternTree node, P p) {
        return null;
    }

    @Override
    default R visitDefaultCaseLabel(DefaultCaseLabelTree node, P p) {
        return null;
    }

    @Override
    default R visitConstantCaseLabel(ConstantCaseLabelTree node, P p) {
        return null;
    }

    @Override
    default R visitPatternCaseLabel(PatternCaseLabelTree node, P p) {
        return null;
    }

    @Override
    default R visitDeconstructionPattern(DeconstructionPatternTree node, P p) {
        return null;
    }

    @Override
    default R visitMethod(MethodTree node, P p) {
        return null;
    }

    @Override
    default R visitModifiers(ModifiersTree node, P p) {
        return null;
    }

    @Override
    default R visitNewArray(NewArrayTree node, P p) {
        return null;
    }

    @Override
    default R visitNewClass(NewClassTree node, P p) {
        return null;
    }

    @Override
    default R visitLambdaExpression(LambdaExpressionTree node, P p) {
        return null;
    }

    @Override
    default R visitPackage(PackageTree node, P p) {
        return null;
    }

    @Override
    default R visitParenthesized(ParenthesizedTree node, P p) {
        return null;
    }

    @Override
    default R visitReturn(ReturnTree node, P p) {
        return null;
    }

    @Override
    default R visitMemberSelect(MemberSelectTree node, P p) {
        return null;
    }

    @Override
    default R visitMemberReference(MemberReferenceTree node, P p) {
        return null;
    }

    @Override
    default R visitEmptyStatement(EmptyStatementTree node, P p) {
        return null;
    }

    @Override
    default R visitSwitch(SwitchTree node, P p) {
        return null;
    }

    @Override
    default R visitSwitchExpression(SwitchExpressionTree node, P p) {
        return null;
    }

    @Override
    default R visitSynchronized(SynchronizedTree node, P p) {
        return null;
    }

    @Override
    default R visitThrow(ThrowTree node, P p) {
        return null;
    }

    @Override
    default R visitCompilationUnit(CompilationUnitTree node, P p) {
        return null;
    }

    @Override
    default R visitTry(TryTree node, P p) {
        return null;
    }

    @Override
    default R visitParameterizedType(ParameterizedTypeTree node, P p) {
        return null;
    }

    @Override
    default R visitUnionType(UnionTypeTree node, P p) {
        return null;
    }

    @Override
    default R visitIntersectionType(IntersectionTypeTree node, P p) {
        return null;
    }

    @Override
    default R visitArrayType(ArrayTypeTree node, P p) {
        return null;
    }

    @Override
    default R visitTypeCast(TypeCastTree node, P p) {
        return null;
    }

    @Override
    default R visitPrimitiveType(PrimitiveTypeTree node, P p) {
        return null;
    }

    @Override
    default R visitTypeParameter(TypeParameterTree node, P p) {
        return null;
    }

    @Override
    default R visitInstanceOf(InstanceOfTree node, P p) {
        return null;
    }

    @Override
    default R visitUnary(UnaryTree node, P p) {
        return null;
    }

    @Override
    default R visitVariable(VariableTree node, P p) {
        return null;
    }

    @Override
    default R visitWhileLoop(WhileLoopTree node, P p) {
        return null;
    }

    @Override
    default R visitWildcard(WildcardTree node, P p) {
        return null;
    }

    @Override
    default R visitModule(ModuleTree node, P p) {
        return null;
    }

    @Override
    default R visitExports(ExportsTree node, P p) {
        return null;
    }

    @Override
    default R visitOpens(OpensTree node, P p) {
        return null;
    }

    @Override
    default R visitProvides(ProvidesTree node, P p) {
        return null;
    }

    @Override
    default R visitRequires(RequiresTree node, P p) {
        return null;
    }

    @Override
    default R visitUses(UsesTree node, P p) {
        return null;
    }

    @Override
    default R visitOther(Tree node, P p) {
        return null;
    }

    @Override
    default R visitYield(YieldTree node, P p) {
        return null;
    }
}
