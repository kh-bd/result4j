package dev.khbd.result4j.javac;

import com.sun.tools.javac.tree.JCTree;

/**
 * @author Sergei Khadanovich
 */
interface PrettyPrinter {

    /**
     * Print tree.
     *
     * @param tree ast
     */
    void print(JCTree tree);
}
