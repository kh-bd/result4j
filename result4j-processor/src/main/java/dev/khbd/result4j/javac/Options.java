package dev.khbd.result4j.javac;

import lombok.Value;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author Sergei_Khadanovich
 */
class Options {

    private static final OptionsKey<Boolean> PRETTY_PRINTING_ENABLED = new OptionsKey<>("prettyPrint");

    private static final List<OptionsDescription<?>> DESCRIPTIONS = List.of(
            new OptionsDescription<>(PRETTY_PRINTING_ENABLED, Boolean::parseBoolean, () -> false)
    );

    private final Map<OptionsKey<?>, Object> params = new HashMap<>();

    Options(String... args) {
        for (String arg : args) {
            String[] parts = arg.split("=");
            if (parts.length != 2) {
                continue;
            }
            OptionsDescription<?> description = findDescription(new OptionsKey<>(parts[0]));
            if (Objects.isNull(description)) {
                continue;
            }
            params.put(description.getKey(), parseValueOrDefault(description, parts[1]));
        }
        for (OptionsDescription<?> description : DESCRIPTIONS) {
            params.putIfAbsent(description.getKey(), description.getDefaultValue().get());
        }
    }

    private static OptionsDescription<?> findDescription(OptionsKey<?> key) {
        return DESCRIPTIONS.stream()
                .filter(description -> description.getKey().equals(key))
                .findFirst()
                .orElse(null);
    }

    private static Object parseValueOrDefault(OptionsDescription<?> description, String value) {
        try {
            return description.getParser().apply(value);
        } catch (Exception e) {
            return description.getDefaultValue().get();
        }
    }

    /**
     * Is pretty printing enabled or not.
     */
    boolean prettyPrintEnabled() {
        return getKeyValue(PRETTY_PRINTING_ENABLED);
    }

    @SuppressWarnings("unchecked")
    private <T> T getKeyValue(OptionsKey<T> key) {
        return (T) params.get(key);
    }

    @Value
    private static class OptionsKey<V> {
        String name;
    }

    @Value
    private static class OptionsDescription<V> {
        OptionsKey<V> key;
        Function<String, ? extends V> parser;
        Supplier<? extends V> defaultValue;
    }

}
