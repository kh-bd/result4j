package dev.khbd.result4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import java.util.NoSuchElementException;

/**
 * @author Sergei Khadanovich
 */
public class OptionTest {

    @Test
    public void isEmpty_optionIsNone_returnTrue() {
        boolean result = Option.none().isEmpty();

        assertThat(result).isTrue();
    }

    @Test
    public void isEmpty_optionIsNotNone_returnFalse() {
        boolean result = Option.some("Alex").isEmpty();

        assertThat(result).isFalse();
    }

    @Test(expectedExceptions = NoSuchElementException.class)
    public void get_optionIsNone_throwError() {
        Option<String> option = Option.none();

        option.get();
    }

    @Test
    public void get_optionIsNotNone_returnValue() {
        Option<String> option = Option.some("Alex");

        String result = option.get();

        assertThat(result).isEqualTo("Alex");
    }

    @Test
    public void filter_valueIsEmpty_returnNone() {
        Option<String> option = Option.none();

        Option<String> result = option.filter(__ -> true);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void filter_valueIsSomeButPredicateFalse_returnNone() {
        Option<String> option = Option.some("Alex");

        Option<String> result = option.filter(__ -> false);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void filter_valueIsSomeAndPredicateTrue_returnSome() {
        Option<String> option = Option.some("Alex");

        Option<String> result = option.filter(__ -> true);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void fromNullable_valueIsNull_returnNone() {
        Option<?> result = Option.fromNullable(null);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void fromNullable_valueIsNotNull_returnSome() {
        Option<?> result = Option.fromNullable("Alex");

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void flatten_outerIsNone_returnEmpty() {
        Option<String> result = Option.flatten(Option.none());

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void flatten_innerInNone_returnEmpty() {
        Option<String> result = Option.flatten(Option.some(Option.none()));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void flatten_bothIsSome_returnSome() {
        Option<String> result = Option.flatten(Option.some(Option.some("Alex")));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }
}