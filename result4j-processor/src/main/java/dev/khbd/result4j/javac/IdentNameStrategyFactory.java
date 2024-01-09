package dev.khbd.result4j.javac;

import com.sun.tools.javac.util.Context;

/**
 * Ident strategy factory.
 *
 * @author Sergei Khadanovich
 */

interface IdentNameStrategyFactory {

    Context.Key<IdentNameStrategyFactory> FACTORY_KEY = new Context.Key<>();

    /**
     * Create new ident strategy instance.
     */
    IdentNameStrategy create();

    /**
     * Factory method to create factory singleton and insert it into context.
     */
    static IdentNameStrategyFactory instance(Context context) {
        IdentNameStrategyFactory instance = context.get(FACTORY_KEY);
        if (instance == null) {
            instance = new IncrementIdentNameStrategyFactory();
            context.put(FACTORY_KEY, instance);
        }
        return instance;
    }
}

/**
 * Ident name strategy.
 */
interface IdentNameStrategy {

    /**
     * Get ident name.
     */
    String getName(String name);
}
