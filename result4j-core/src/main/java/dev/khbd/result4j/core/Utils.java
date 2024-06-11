package dev.khbd.result4j.core;

import lombok.experimental.UtilityClass;

/**
 * @author Sergei Khadanovich
 */
@UtilityClass
class Utils {

    static String ERROR_MESSAGE =
            "This is a method with special support at compile time.\n" +
            "If you see this message, unwrap call was not transformed during compilation.\n" +
            "Check your compiler plugin configuration and feel free to open an issue https://github.com/kh-bd/result4j\n" +
            "if you need any help:)";

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
