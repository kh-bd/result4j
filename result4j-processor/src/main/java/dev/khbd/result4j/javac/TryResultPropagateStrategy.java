package dev.khbd.result4j.javac;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;

/**
 * Propagate strategy for {@link dev.khbd.result4j.core.Try} data type.
 *
 * @author Sergei Khadanovich
 */
class TryResultPropagateStrategy implements ResultPropagateStrategy {

    @Override
    public String id() {
        return "try";
    }

    @Override
    public Symbol type(Context context) {
        Symtab symtab = Symtab.instance(context);
        Names names = Names.instance(context);
        return symtab.enterClass(symtab.unnamedModule, names.fromString("dev.khbd.result4j.core.Try"));
    }

    @Override
    public PropagateLogicBuilder propagateLogicBuilder(Context context) {
        return new TryPropagateLogicBuilder(context);
    }

    /**
     * Propagate logic builder for {@link dev.khbd.result4j.core.Try} data type.
     *
     * @author Sergei Khadanovich
     */
    private static class TryPropagateLogicBuilder implements PropagateLogicBuilder {

        private final Names names;
        private final TreeMaker treeMaker;
        private final IdentNameStrategyFactory nameStrategyFactory;

        TryPropagateLogicBuilder(Context context) {
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
                            treeMaker.Apply(List.nil(), treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("isFailure")), List.nil()),
                            treeMaker.Return(
                                    treeMaker.Apply(
                                            List.nil(),
                                            treeMaker.Select(treeMaker.Ident(names.fromString("Try")), names.fromString("failure")),
                                            List.of(
                                                    treeMaker.Apply(
                                                            List.nil(),
                                                            treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("getError")),
                                                            List.nil()
                                                    )
                                            )
                                    )
                            ),
                            null),
                    treeMaker.VarDef(treeMaker.Modifiers(0), receiverValueName, null,
                            treeMaker.Apply(List.nil(), treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("get")), List.nil()),
                            true
                    )
            );

            return new PropagateLogic(statements, treeMaker.Ident(receiverValueName));
        }
    }
}
