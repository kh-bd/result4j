package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Names;
import lombok.RequiredArgsConstructor;

/**
 * Propagate logic builder for {@link dev.khbd.result4j.core.Option} data type.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class OptionPropagateLogicBuilder implements PropagateLogicBuilder {

    private final Names names;
    private final TreeMaker treeMaker;
    private final IdentNameStrategyFactory nameStrategyFactory;

    OptionPropagateLogicBuilder(Context context,
                                IdentNameStrategyFactory nameStrategyFactory) {
        this.names = Names.instance(context);
        this.treeMaker = TreeMaker.instance(context);
        this.nameStrategyFactory = nameStrategyFactory;
    }

    @Override
    public PropagateLogic build(JCTree.JCExpression receiver, int position) {
        IdentNameStrategy nameStrategy = nameStrategyFactory.create();

        var receiverName = names.fromString(nameStrategy.getName("$$receiver"));
        var receiverValueName = names.fromString(nameStrategy.getName("$$receiverValue"));

        treeMaker.at(position);

        List<JCTree.JCStatement> statements = List.of(
                treeMaker.VarDef(treeMaker.Modifiers(0), receiverName, null, receiver, true),
                treeMaker.If(
                        treeMaker.Apply(List.nil(), treeMaker.Select(treeMaker.Ident(receiverName), names.fromString("isEmpty")), List.nil()),
                        treeMaker.Return(
                                treeMaker.Apply(List.nil(),
                                        treeMaker.Select(treeMaker.Ident(names.fromString("Option")), names.fromString("none")), List.nil()
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
