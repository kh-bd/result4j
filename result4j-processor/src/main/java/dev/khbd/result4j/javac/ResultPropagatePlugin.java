package dev.khbd.result4j.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;

/**
 * @author Sergei_Khadanovich
 */
public class ResultPropagatePlugin implements Plugin {

    private static final List<ResultPropagateStrategy> STRATEGIES = List.of(
            new OptionResultPropagateStrategy(),
            new TryResultPropagateStrategy(),
            new EitherResultPropagateStrategy()
    );

    @Override
    public String getName() {
        return "result4j";
    }

    @Override
    public void init(JavacTask task, String... args) {
        Options options = new Options(args);
        PrettyPrinter printer = options.prettyPrintEnabled() ? new StdoutPrettyPrinter() : new NoOpsPrettyPrinter();

        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent event) {
                if (event.getKind() != TaskEvent.Kind.ANALYZE) {
                    return;
                }

                Context context = ((BasicJavacTask) task).getContext();

                BundleInitializer.initPluginBundles(context);

                CompilationUnitTree unit = event.getCompilationUnit();

                StatementProcessingTreeScanner scanner = buildStatementProcessingTreeScanner(context);
                ReAttributer attributer = new ReAttributer(context);

                int times = 0;
                while (unit.accept(scanner, null)) {
                    attributer.attribute((JCTree.JCCompilationUnit) unit);
                    times++;
                }

                if (times > 0) {
                    printer.print((JCTree.JCCompilationUnit) unit);
                }

                Logger logger = new Logger(Log.instance(context), JCDiagnostic.Factory.instance(context), unit.getSourceFile());
                RemainedUnwrapCallAnnotator unwrapCallAnnotator =
                        new RemainedUnwrapCallAnnotator(logger, getAllSupportedTypes(context));
                unwrapCallAnnotator.scan(unit, null);
            }
        });
    }

    private static StatementProcessingTreeScanner buildStatementProcessingTreeScanner(Context context) {
        List<StatementProcessor> processors =
                STRATEGIES.map(strategy -> new UnwrapCallReplacerStatementProcessor(
                        new UnwrapCallSearcher(strategy.type(context)), strategy.propagateLogicBuilder(context)));
        return new StatementProcessingTreeScanner(processors, context);
    }

    private static List<Symbol> getAllSupportedTypes(Context context) {
        return STRATEGIES.map(strategy -> strategy.type(context));
    }

}
