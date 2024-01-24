package dev.khbd.result4j.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Tree;
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
import lombok.RequiredArgsConstructor;

import javax.tools.Diagnostic;

/**
 * @author Sergei_Khadanovich
 */
public class ResultPropagatePlugin implements Plugin {

    private static final List<ResultPropagateStrategy> STRATEGIES = List.of(
            new OptionResultPropagateStrategy(),
            new TryResultPropagateStrategy(),
            new EitherResultPropagateStrategy(),
            new OptionalResultPropagateStrategy()
    );

    @Override
    public String getName() {
        return "result4j";
    }

    @Override
    public void init(JavacTask task, String... args) {
        task.addTaskListener(new ResultPropagateTaskListener(task, new Options(args)));
    }

    @RequiredArgsConstructor
    private static class ResultPropagateTaskListener implements TaskListener {

        private final Context context;

        private final PrettyPrinter printer;
        private final ReAttributer attributer;

        private FilterDiagnosticHandler diagnosticHandler;

        ResultPropagateTaskListener(JavacTask task, Options options) {
            this.context = ((BasicJavacTask) task).getContext();
            this.printer = options.prettyPrintEnabled() ? new StdoutPrettyPrinter() : new NoOpsPrettyPrinter();
            this.attributer = new ReAttributer(context);

            BundleInitializer.initPluginBundles(context);
        }

        @Override
        public void started(TaskEvent event) {
            if (event.getKind() != TaskEvent.Kind.ANALYZE) {
                return;
            }

//            installDiagnostic();
        }

        @Override
        public void finished(TaskEvent event) {
            if (event.getKind() != TaskEvent.Kind.ANALYZE) {
                return;
            }

//            clearDiagnostic();

            StatementProcessingTreeScanner scanner = buildStatementProcessingTreeScanner(context);

            CompilationUnitTree unit = event.getCompilationUnit();

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

        private void installDiagnostic() {
            diagnosticHandler = new FilterDiagnosticHandler(Log.instance(context), getAllSupportedTypes(context));
        }

        private void clearDiagnostic() {
            Log.instance(context).popDiagnosticHandler(diagnosticHandler);
            diagnosticHandler = null;
        }
    }

    private static class FilterDiagnosticHandler extends Log.DiagnosticHandler {

        private final List<Symbol> types;

        FilterDiagnosticHandler(Log log, List<Symbol> types) {
            this.types = types;

            install(log);
        }

        @Override
        public void report(JCDiagnostic diag) {
            System.out.println("reported " + diag);

            if (diag.getKind() != Diagnostic.Kind.ERROR) {
                prev.report(diag);
                return;
            }

            var tree = diag.getDiagnosticPosition().getTree();
            if (tree.getKind() != Tree.Kind.MEMBER_SELECT) {
                prev.report(diag);
                return;
            }

            JCTree.JCFieldAccess fieldAccess = (JCTree.JCFieldAccess) tree;
            if (!fieldAccess.name.contentEquals("unwrap")) {
                prev.report(diag);
                return;
            }

            if (!oneOfSupportedType(fieldAccess.selected)) {
                prev.report(diag);
            }
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

    private static StatementProcessingTreeScanner buildStatementProcessingTreeScanner(Context context) {
        List<StatementProcessor> processors =
                STRATEGIES.map(strategy -> new UnwrapCallReplacerStatementProcessor(
                        new UnwrapCallSearcher(strategy.type(context)), strategy.propagateLogicBuilder(context)));
        return new StatementProcessingTreeScanner(processors, context);
    }

    private static List<Symbol> getAllSupportedTypes(Context context) {
        return STRATEGIES.map(strategy -> strategy.type(context));
    }

    @Override
    public boolean autoStart() {
        return true;
    }
}
