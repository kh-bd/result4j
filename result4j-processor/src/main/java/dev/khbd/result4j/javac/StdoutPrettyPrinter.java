package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;
import com.sun.tools.javac.tree.Pretty;

import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * @author Sergei Khadanovich
 */
public class StdoutPrettyPrinter implements PrettyPrinter {

    @Override
    public void print(JCTree tree) {
        OutputStreamWriter writer = new OutputStreamWriter(System.out);

        Pretty pretty = new Pretty(writer, true);
        tree.accept(pretty);

        try {
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException ioe) {
            // ops
            ioe.printStackTrace();
        }
    }
}
