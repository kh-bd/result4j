package dev.khbd.result4j.javac;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.JavacTask;
import com.sun.source.util.Plugin;
import com.sun.source.util.TaskEvent;
import com.sun.source.util.TaskListener;
import com.sun.tools.javac.api.BasicJavacTask;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.JCDiagnostic;
import com.sun.tools.javac.util.List;
import com.sun.tools.javac.util.Log;
import com.sun.tools.javac.util.Names;

/**
 * @author Sergei_Khadanovich
 */
public class ResultPropagatePlugin implements Plugin {

    @Override
    public String getName() {
        return "result4j";
    }

    @Override
    public void init(JavacTask task, String... args) {
        Options options = new Options(args);
        PrettyPrinter printer = options.prettyPrintEnabled() ? new StdoutPrettyPrinter() : new NoOpsPrettyPrinter();

        IdentNameStrategyFactory nameStrategyFactory = new IncrementIdentNameStrategyFactory();

        task.addTaskListener(new TaskListener() {
            @Override
            public void finished(TaskEvent event) {
                if (event.getKind() != TaskEvent.Kind.ANALYZE) {
                    return;
                }

                Context context = ((BasicJavacTask) task).getContext();

                BundleInitializer.initPluginBundles(context);

                CompilationUnitTree unit = event.getCompilationUnit();

                SupportedTypes supportedTypes = getSupportedTypes(context);

                UnwrapCallReplacerStatementProcessor optionProcessor = new UnwrapCallReplacerStatementProcessor(
                        new UnwrapCallSearcher(supportedTypes.option()),
                        new OptionPropagateLogicBuilder(context, nameStrategyFactory)
                );

                StatementProcessingTreeScanner scanner = new StatementProcessingTreeScanner(List.of(
                        optionProcessor
                ));

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
                RemainedUnwrapCallAnnotator unwrapCallAnnotator = new RemainedUnwrapCallAnnotator(logger, supportedTypes.toList());
                unwrapCallAnnotator.scan(unit, null);
            }
        });
    }

    private static SupportedTypes getSupportedTypes(Context context) {
        Symtab symtab = Symtab.instance(context);
        Names names = Names.instance(context);
        return new SupportedTypes(
                symtab.enterClass(symtab.unnamedModule, names.fromString("dev.khbd.result4j.core.Option"))
        );
    }

    private record SupportedTypes(Symbol option) {

        List<Symbol> toList() {
            return List.of(option);
        }
    }

    @Override
    public boolean autoStart() {
        return true;
    }
}
