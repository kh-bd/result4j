package dev.khbd.result4j.javac;

import lombok.RequiredArgsConstructor;

/**
 * Strategy factory which append UUID at the end of each name.
 *
 * @author Sergei Khadanovich
 */
class IncrementIdentNameStrategyFactory implements IdentNameStrategyFactory {

    private long count = 0;

    @Override
    public IdentNameStrategy create() {
        return new SuffixedIdentNameStrategy("_" + count++);
    }

    @RequiredArgsConstructor
    private static class SuffixedIdentNameStrategy implements IdentNameStrategy {

        private final String suffix;

        @Override
        public String getName(String name) {
            return name + suffix;
        }
    }
}
