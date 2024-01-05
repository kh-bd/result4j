package dev.khbd.result4j.javac;

import lombok.RequiredArgsConstructor;

import java.util.UUID;

/**
 * Strategy factory which append UUID at the end of each name.
 *
 * @author Sergei Khadanovich
 */
class UUIDIdentNameStrategyFactory implements IdentNameStrategyFactory {

    @Override
    public IdentNameStrategy create() {
        String suffix = UUID.randomUUID().toString().replace("-", "");
        return new SuffixedIdentNameStrategy("_" + suffix);
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
