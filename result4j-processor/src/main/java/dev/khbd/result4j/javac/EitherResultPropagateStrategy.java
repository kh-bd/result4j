package dev.khbd.result4j.javac;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

/**
 * Propagate strategy for {@link dev.khbd.result4j.core.Either} data type.
 *
 * @author Sergei Khadanovich
 */
class EitherResultPropagateStrategy implements ResultPropagateStrategy {

    @Override
    public String id() {
        return "either";
    }

    @Override
    public Symbol type(Context context) {
        Symtab symtab = Symtab.instance(context);
        Names names = Names.instance(context);
        return symtab.enterClass(symtab.unnamedModule, names.fromString("dev.khbd.result4j.core.Either"));
    }

    @Override
    public PropagateLogicBuilder propagateLogicBuilder(Context context) {
        return new EitherPropagateLogicBuilder(context);
    }

    /**
     * Propagate logic builder for {@link dev.khbd.result4j.core.Either} data type.
     *
     * @author Sergei Khadanovich
     */
    private static class EitherPropagateLogicBuilder implements PropagateLogicBuilder {

        private final Names names;
        private final TreeMaker treeMaker;
        private final IdentNameStrategyFactory nameStrategyFactory;

        EitherPropagateLogicBuilder(Context context) {
            this.names = Names.instance(context);
            this.treeMaker = TreeMaker.instance(context);
            this.nameStrategyFactory = IdentNameStrategyFactory.instance(context);
        }

        @Override
        public PropagateLogic build(JCTree.JCExpression receiver, int position) {
            IdentNameStrategy nameStrategy = nameStrategyFactory.create();

            var receiverName = names.fromString(nameStrategy.getName("$$rec"));
            var receiverValueName = names.fromString(nameStrategy.getName("$$recVal"));

            treeMaker.at(position);

            List<JCTree.JCStatement> statements = List.of(
                    treeMaker.VarDef(treeMaker.Modifiers(0), receiverName, null, receiver, true),
                    treeMaker.If(
                            treeMaker.Apply(List.nil(), treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("isLeft")), List.nil()),
                            treeMaker.Return(
                                    treeMaker.Apply(
                                            List.nil(),
                                            treeMaker.Select(treeMaker.Ident(names.fromString("Either")), names.fromString("left")),
                                            List.of(
                                                    treeMaker.Apply(
                                                            List.nil(),
                                                            treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("getLeft")),
                                                            List.nil()
                                                    )
                                            )
                                    )
                            ),
                            null),
                    treeMaker.VarDef(treeMaker.Modifiers(0), receiverValueName, null,
                            treeMaker.Apply(List.nil(), treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("getRight")), List.nil()),
                            true
                    )
            );

            return new PropagateLogic(statements, treeMaker.Ident(receiverValueName));
        }
    }
}
