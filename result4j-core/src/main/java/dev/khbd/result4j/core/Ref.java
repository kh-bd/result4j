package dev.khbd.result4j.core;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * Helper box class to implement collectors.
 *
 * @author Sergei Khadanovich
 */
@Data
@AllArgsConstructor
class Ref<R> {

    R ref;
}
