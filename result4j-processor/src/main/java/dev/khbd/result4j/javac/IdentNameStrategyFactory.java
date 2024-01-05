package dev.khbd.result4j.javac;

/**
 * Ident strategy factory.
 *
 * @author Sergei Khadanovich
 */

interface IdentNameStrategyFactory {

    /**
     * Create new ident strategy instance.
     */
    IdentNameStrategy create();
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
