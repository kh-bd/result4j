package dev.khbd.result4j.core;

import lombok.Value;

/**
 * A pair consisting of two elements.
 *
 * @param <L> left value type
 * @param <R> right value type
 * @author Alexey_Bodyak
 */
@Value(staticConstructor = "of")
public class Pair<L, R> {
    L left;
    R right;
}