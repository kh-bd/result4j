package dev.khbd.result4j.core;

import lombok.experimental.UtilityClass;

/**
 * @author Sergei Khadanovich
 */
@UtilityClass
class Utils {

    /**
     * Unchecked cast value to another type.
     *
     * @param source value
     * @return the same value
     */
    @SuppressWarnings("unchecked")
    public static <S, T> T cast(S source) {
        return (T) source;
    }
}
