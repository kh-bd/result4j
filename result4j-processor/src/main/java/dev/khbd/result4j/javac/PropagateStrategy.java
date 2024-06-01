package dev.khbd.result4j.javac;

import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.util.Context;

/**
 * Base interface to extend propagate plugin.
 *
 * @author Sergei Khadanovich
 */
interface PropagateStrategy {

    /**
     * Strategy id.
     */
    String id();

    /**
     * Type to process.
     */
    Symbol type(Context context);

    /**
     * Factory method to create propagate logic for supported type.
     */
    PropagateLogicBuilder propagateLogicBuilder(Context context);
}
