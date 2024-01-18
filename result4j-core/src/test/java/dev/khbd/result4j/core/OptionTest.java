package dev.khbd.result4j.core;

import static org.assertj.core.api.Assertions.assertThat;

import org.testng.annotations.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * @author Sergei Khadanovich
 */
public class OptionTest {

    @Test
    public void drop_valueIsEmpty_returnEmpty() {
        Option<Object> value = Option.none();

        Option<NoData> result = value.drop();

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void drop_valueIsNotEmpty_returnNotEmptyWithNoData() {
        Option<String> value = Option.some("Alex");

        Option<NoData> result = value.drop();

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo(NoData.INSTANCE);
    }

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

    @Test
    public void map_valueIsNone_returnNone() {
        Option<String> option = Option.none();

        Option<Integer> result = option.map(String::length);

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void map_valueIsSome_returnTransformedSome() {
        Option<String> option = Option.some("Alex");

        Option<Integer> result = option.map(String::length);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo(4);
    }

    @Test
    public void flatmap_valueIsNone_returnNone() {
        Option<String> option = Option.none();

        Option<Integer> result = option.flatMap(str -> Option.some(str.length()));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void flatmap_valueIsSomeAndFunctionReturnNone_returnNone() {
        Option<String> option = Option.some("Alex");

        Option<Integer> result = option.flatMap(str -> Option.none());

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void flatmap_valueIsSomeAndFunctionReturnSome_returnSome() {
        Option<String> option = Option.some("Alex");

        Option<Integer> result = option.flatMap(str -> Option.some(str.length()));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo(4);
    }

    @Test
    public void toOptional_valueIsNone_returnEmptyOptional() {
        Option<Object> option = Option.none();

        Optional<Object> result = option.toOptional();

        assertThat(result).isEmpty();
    }

    @Test
    public void toOptional_valueIsSome_returnNotEmptyOptional() {
        Option<String> option = Option.some("Alex");

        Optional<String > result = option.toOptional();

        assertThat(result).hasValue("Alex");
    }

    @Test
    public void fromOptional_valueIsEmpty_returnNone() {
        Option<Object> result = Option.fromOptional(Optional.empty());

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void fromOptional_valueIsNotEmpty_returnSome() {
        Option<String> result = Option.fromOptional(Optional.of("Alex"));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void traverse_emptyList_returnSomeWithEmptyList() {
        Option<List<Boolean>> result = Option.traverse(List.of(), __ -> Option.some(true));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEmpty();
    }

    @Test
    public void traverse_notEmptyListAndAllResultsAreSome_returnSome() {
        Option<List<Integer>> result = Option.traverse(List.of("1", "2", "3"), str -> Option.some(Integer.parseInt(str)));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    public void traverse_notEmptyListAndSomeValuesAreNone_returnNone() {
        Option<List<Integer>> result = Option.traverse(List.of("1", "2", "3"), str -> {
            if (str.equals("3")) {
                return Option.none();
            }
            return Option.some(Integer.parseInt(str));
        });

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void sequence_emptyList_returnSomeWithEmptyList() {
        Option<List<Boolean>> result = Option.sequence(List.of());

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEmpty();
    }

    @Test
    public void sequence_notEmptyListAndAllValuesAreSome_returnSome() {
        Option<List<Integer>> result = Option.sequence(List.of(Option.some(1), Option.some(2), Option.some(3)));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    public void sequence_notEmptyListAndSomeValuesAreNone_returnNone() {
        Option<List<Integer>> result = Option.sequence(List.of(Option.some(1), Option.none(), Option.some(3)));

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void orElse_valueIsNone_returnOther() {
        Option<String> option = Option.none();

        Option<String> result = option.orElse(Option.some("Alex"));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void orElse_valueIsSome_returnThis() {
        Option<String> option = Option.some("Alex");

        Option<String> result = option.orElse(Option.none());

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void orElseLazy_valueIsNone_returnOther() {
        Option<String> option = Option.none();

        Option<String> result = option.orElse(() -> Option.some("Alex"));

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void orElseLazy_valueIsSome_returnThis() {
        Option<String> option = Option.some("Alex");

        Option<String> result = option.orElse(Option::none);

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo("Alex");
    }

    @Test
    public void toStream_optionIsEmpty_returnEmptyStream() {
        Option<Object> option = Option.none();

        Stream<Object> result = option.toStream();

        assertThat(result).isEmpty();
    }

    @Test
    public void toStream_optionIsNotEmpty_returnStreamOneElement() {
        Option<String> option = Option.some("Alex");

        Stream<String> result = option.toStream();

        assertThat(result).containsExactly("Alex");
    }
}