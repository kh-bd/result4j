package dev.khbd.result4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;

/**
 * @author Sergei Khadanovich
 */
public class EitherTest {

    @Test
    public void drop_valueIsLeft_returnSame() {
        Either<String, Object> value = Either.left("error");

        Either<String, NoData> result = value.drop();

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void drop_valueIsRight_dropValue() {
        Either<Object, String> value = Either.right("Alex");

        Either<Object, NoData> result = value.drop();

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(NoData.INSTANCE);
    }

    @Test
    public void dropRight_valueIsLeft_returnSame() {
        Either<String, Object> value = Either.left("error");

        Either<String, NoData> result = value.dropRight();

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void dropRight_valueIsRight_dropValue() {
        Either<Object, String> value = Either.right("Alex");

        Either<Object, NoData> result = value.dropRight();

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(NoData.INSTANCE);
    }

    @Test
    public void dropLeft_valueIsLeft_dropLeftValue() {
        Either<String, Object> value = Either.left("error");

        Either<NoData, Object> result = value.dropLeft();

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(NoData.INSTANCE);
    }

    @Test
    public void dropLeft_valueIsRight_returnSame() {
        Either<Object, String> value = Either.right("Alex");

        Either<NoData, String> result = value.dropLeft();

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("Alex");
    }

    @Test
    public void toLeftStream_eitherIsLeft_returnOneElementStream() {
        Either<String, Integer> either = Either.left("ops");

        Stream<String> result = either.leftToStream();

        assertThat(result).containsExactlyInAnyOrder("ops");
    }

    @Test
    public void toLeftStream_eitherIsRight_returnEmptyStream() {
        Either<String, Integer> either = Either.right(1);

        Stream<String> result = either.leftToStream();

        assertThat(result).isEmpty();
    }

    @Test
    public void toRightStream_eitherIsRight_returnOneElementStream() {
        Either<?, String> either = Either.right("ops");

        Stream<String> result = either.rightToStream();

        assertThat(result).containsExactlyInAnyOrder("ops");
    }

    @Test
    public void toRightStream_eitherIsLeft_returnEmptyStream() {
        Either<String, ?> either = Either.left("ops");

        Stream<?> result = either.rightToStream();

        assertThat(result).isEmpty();
    }

    @Test
    public void leftFromOptionLazy_valueIsEmpty_returnRight() {
        Either<String, ?> either = Either.leftFromOption(Option.none(), () -> "Option is empty");

        assertThat(either.isRight()).isTrue();
        assertThat(either.getRight()).isEqualTo("Option is empty");
    }

    @Test
    public void leftFromOption_valueIsEmpty_returnRight() {
        Either<String, ?> either = Either.leftFromOption(Option.none(), "Option is empty");

        assertThat(either.isRight()).isTrue();
        assertThat(either.getRight()).isEqualTo("Option is empty");
    }

    @Test
    public void fromOptionLazy_valueIsEmpty_returnLeft() {
        Either<String, String> either = Either.fromOption(Option.none(), () -> "Option is empty");

        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo("Option is empty");
    }

    @Test
    public void fromOptionLazy_valueIsNotEmpty_returnRight() {
        Either<String, String> either = Either.fromOption(Option.some("hello"), () -> "Option is empty");

        assertThat(either.isRight()).isTrue();
        assertThat(either.getRight()).isEqualTo("hello");
    }

    @Test
    public void fromOption_valueIsEmpty_returnLeft() {
        Either<String, String> either = Either.fromOption(Option.none(), "Option is empty");

        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo("Option is empty");
    }

    @Test
    public void fromOption_valueIsNotEmpty_returnRight() {
        Either<String, String> either = Either.fromOption(Option.some("hello"), "Option is empty");

        assertThat(either.isRight()).isTrue();
        assertThat(either.getRight()).isEqualTo("hello");
    }

    @Test
    public void fromNullable_valueIsNull_returnLeft() {
        Either<String, String> either = Either.fromNullable(null, () -> "Element is null");

        assertThat(either.isLeft()).isTrue();
        assertThat(either.getLeft()).isEqualTo("Element is null");
    }

    @Test
    public void fromNullable_valueIsNotNull_returnRight() {
        Either<String, String> either = Either.fromNullable("hello", () -> "element is null");

        assertThat(either.isRight()).isTrue();
        assertThat(either.getRight()).isEqualTo("hello");
    }

    @Test
    public void filterLeft_valueIsLeftAndGood_returnLeft() {
        Either<String, Integer> either = Either.left("pss");

        var result = either.leftFilter(str -> str.equals("pss"), () -> 10);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("pss");
    }

    @Test
    public void filterLeft_valueIsLeftButBad_returnRight() {
        Either<String, Integer> either = Either.left("pss");

        var result = either.leftFilter(str -> !str.equals("pss"), () -> 10);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(10);
    }

    @Test
    public void filterLeft_valueIsRight_returnRight() {
        Either<String, Integer> either = Either.right(10);

        var result = either.leftFilter(str -> str.equals("pss"), () -> 20);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(10);
    }

    @Test
    public void filterRight_valueIsRightAndGood_returnRight() {
        var either = Either.right(10);

        var result = either.rightFilter(i -> i == 10, () -> "not a ten");

        assertThat(result.isRight()).isTrue();
    }

    @Test
    public void filterRight_valueIsRightButNotGood_returnLeft() {
        var either = Either.right(10);

        var result = either.rightFilter(i -> i != 10, () -> "not a ten");

        assertThat(result.isRight()).isFalse();
        assertThat(result.getLeft()).isEqualTo("not a ten");
    }

    @Test
    public void filterRight_valueIsLeft_returnLeft() {
        Either<String, Integer> either = Either.left("is left");

        var result = either.rightFilter(i -> i != 10, () -> "not a ten");

        assertThat(result.isRight()).isFalse();
        assertThat(result.getLeft()).isEqualTo("is left");
    }

    @Test
    public void toLeftOption_valueIsLeft_returnSome() {
        Option<Integer> result = Either.left(10).leftToOption();

        assertThat(result.get()).isEqualTo(10);
    }

    @Test
    public void toLeftOption_valueIsRight_returnEmpty() {
        Option<Object> result = Either.right(10).leftToOption();

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void toRightOption_valueIsRight_returnSome() {
        Option<Integer> result = Either.right(10).rightToOption();

        assertThat(result.get()).isEqualTo(10);
    }

    @Test
    public void toRightOption_valueIsLeft_returnEmpty() {
        Option<Object> result = Either.left(10).rightToOption();

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void toOption_valueIsRight_returnSome() {
        Option<Integer> result = Either.right(10).toOption();

        assertThat(result.get()).isEqualTo(10);
    }

    @Test
    public void toOption_valueIsLeft_returnEmpty() {
        Option<Object> result = Either.left(10).toOption();

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void isLeft_eitherIsLeft_returnTrue() {
        assertThat(Either.left(1).isLeft()).isTrue();
    }

    @Test
    public void isLeft_eitherIsRight_returnFalse() {
        assertThat(Either.right(1).isLeft()).isFalse();
    }

    @Test
    public void isRight_eitherIsRight_returnTrue() {
        assertThat(Either.right(1).isRight()).isTrue();
    }

    @Test
    public void isRight_eitherIsLeft_returnFalse() {
        assertThat(Either.left(1).isRight()).isFalse();
    }

    @Test
    public void getLeft_eitherIsLeft_returnLeftValue() {
        Either<Integer, ?> either = Either.left(1);

        Integer left = either.getLeft();

        assertThat(left).isEqualTo(1);
    }

    @Test
    public void getLeft_eitherIsRight_throwError() {
        Either<?, Integer> either = Either.right(1);

        Throwable error = catchThrowable(either::getLeft);

        assertThat(error).isInstanceOf(RuntimeException.class)
                .hasMessage("Either is right.");
    }

    @Test
    public void getRight_eitherIsRight_returnRightValue() {
        Either<?, Integer> either = Either.right(1);

        Integer right = either.getRight();

        assertThat(right).isEqualTo(1);
    }

    @Test
    public void getRight_eitherIsLeft_throwError() {
        Either<Integer, ?> either = Either.left(1);

        Throwable error = catchThrowable(either::getRight);

        assertThat(error).isInstanceOf(RuntimeException.class)
                .hasMessage("Either is left.");
    }

    @Test
    public void mapRight_eitherIsLeft_returnSame() {
        Either<Integer, String> either = Either.left(1);

        Either<Integer, Integer> mapped = either.mapRight(String::length);

        assertThat(mapped).isEqualTo(Either.left(1));
    }

    @Test
    public void mapRight_eitherIsRight_returnTransformedValue() {
        Either<Integer, String> either = Either.right("hi");

        Either<Integer, Integer> mapped = either.mapRight(String::length);

        assertThat(mapped).isEqualTo(Either.right(2));
    }

    @Test
    public void map_eitherIsLeft_returnSame() {
        Either<Integer, String> either = Either.left(1);

        Either<Integer, Integer> mapped = either.map(String::length);

        assertThat(mapped).isEqualTo(Either.left(1));
    }

    @Test
    public void map_eitherIsRight_returnTransformedValue() {
        Either<Integer, String> either = Either.right("hi");

        Either<Integer, Integer> mapped = either.map(String::length);

        assertThat(mapped).isEqualTo(Either.right(2));
    }

    @Test
    public void mapLeft_eitherIsLeft_returnTransformed() {
        Either<Integer, String> either = Either.left(1);

        Either<Integer, String> mapped = either.mapLeft(i -> i + 2);

        assertThat(mapped).isEqualTo(Either.left(3));
    }

    @Test
    public void mapLeft_eitherIsRight_returnSame() {
        Either<Integer, String> either = Either.right("hi");

        Either<Integer, String> mapped = either.mapLeft(i -> i + 2);

        assertThat(mapped).isEqualTo(Either.right("hi"));
    }

    @Test
    public void swap_eitherIsRight_returnLeft() {
        Either<Integer, String> either = Either.right("hi");

        Either<String, Integer> result = either.swap();

        assertThat(result).isEqualTo(Either.left("hi"));
    }

    @Test
    public void swap_eitherIsLeft_returnRight() {
        Either<String, Integer> either = Either.left("hi");

        Either<Integer, String> result = either.swap();

        assertThat(result).isEqualTo(Either.right("hi"));
    }

    @Test
    public void ifRight_eitherIsRight_invokeFunction() {
        Either<String, Integer> either = Either.right(1);
        Consumer<Integer> consumer = mock(Consumer.class);

        either.ifRight(consumer);

        verify(consumer, times(1)).accept(1);
    }

    @Test
    public void ifRight_eitherIsLeft_notInvokeFunction() {
        Either<String, Integer> either = Either.left("hello");
        Consumer<Integer> consumer = mock(Consumer.class);

        either.ifRight(consumer);

        verify(consumer, never()).accept(any());
    }

    @Test
    public void ifLeft_eitherIsRight_notInvokeFunction() {
        Either<String, Integer> either = Either.right(1);
        Consumer<String> consumer = mock(Consumer.class);

        either.ifLeft(consumer);

        verify(consumer, never()).accept(any());
    }

    @Test
    public void ifLeft_eitherIsLeft_invokeFunction() {
        Either<String, Integer> either = Either.left("hello");
        Consumer<String> consumer = mock(Consumer.class);

        either.ifLeft(consumer);

        verify(consumer, times(1)).accept("hello");
    }

    @Test
    public void traverseRight_allResultIsRight_returnRight() {
        List<Integer> list = List.of(1, 2, 3);

        Either<?, List<Integer>> result = Either.rightTraverse(list, i -> Either.right(i + 1));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(List.of(2, 3, 4));
    }

    @Test
    public void traverseRight_anyResultIsNotRight_returnLeft() {
        List<Integer> list = List.of(1, 2, 3);

        Either<String, List<Integer>> result = Either.rightTraverse(list, i -> {
            if (i == 2) {
                return Either.left("error");
            }
            return Either.right(i + 1);
        });

        assertThat(result.isRight()).isFalse();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void traverse_allResultIsRight_returnRight() {
        List<Integer> list = List.of(1, 2, 3);

        Either<?, List<Integer>> result = Either.traverse(list, i -> Either.right(i + 1));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(List.of(2, 3, 4));
    }

    @Test
    public void traverse_anyResultIsNotRight_returnLeft() {
        List<Integer> list = List.of(1, 2, 3);

        Either<String, List<Integer>> result = Either.traverse(list, i -> {
            if (i == 2) {
                return Either.left("error");
            }
            return Either.right(i + 1);
        });

        assertThat(result.isRight()).isFalse();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void traverseLeft_anyResultIsRight_returnRight() {
        List<Integer> list = List.of(1, 2, 3);

        Either<List<String>, Integer> result = Either.leftTraverse(list, i -> {
            if (i == 2) {
                return Either.right(i + 1);
            }
            return Either.left("error");
        });

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(3);
    }

    @Test
    public void traverseLeft_allResultIsLeft_returnLeft() {
        List<Integer> list = List.of(1, 2, 3);

        Either<List<String>, Integer> result = Either.leftTraverse(list, i -> Either.left("error"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(List.of("error", "error", "error"));
    }

    @Test
    public void flatMapRight_selfIsRightAndResultIsRight_returnRight() {
        Either<?, String> self = Either.right("hello");

        Either<?, Integer> result = self.flatMapRight(str -> Either.right(str.length()));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(5);
    }

    @Test
    public void flatMapRight_selfIsRightAndResultIsLeft_returnLeft() {
        Either<Integer, String> self = Either.right("hello");

        Either<Integer, Integer> result = self.flatMapRight(str -> Either.left(1));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(1);
    }

    @Test
    public void flatMapRight_selfIsLeft_returnLeft() {
        Either<Integer, String> self = Either.left(1);

        Either<Integer, Integer> result = self.flatMapRight(str -> Either.right(str.length()));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(1);
    }

    @Test
    public void flatMap_selfIsRightAndResultIsRight_returnRight() {
        Either<?, String> self = Either.right("hello");

        Either<?, Integer> result = self.flatMap(str -> Either.right(str.length()));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(5);
    }

    @Test
    public void flatMap_selfIsRightAndResultIsLeft_returnLeft() {
        Either<Integer, String> self = Either.right("hello");

        Either<Integer, Integer> result = self.flatMap(str -> Either.left(1));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(1);
    }

    @Test
    public void flatMap_selfIsLeft_returnLeft() {
        Either<Integer, String> self = Either.left(1);

        Either<Integer, Integer> result = self.flatMap(str -> Either.right(str.length()));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(1);
    }

    @Test
    public void flatMapLeft_selfIsLeftAndResultIsLeft_returnLeft() {
        Either<Integer, String> self = Either.left(1);

        Either<String, String> result = self.flatMapLeft(i -> Either.left(i.toString()));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("1");
    }

    @Test
    public void flatMapLeft_selfIsLeftAndResultIsRight_returnRight() {
        Either<Integer, String> self = Either.left(1);

        Either<Integer, String> result = self.flatMapLeft(i -> Either.right(i.toString()));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("1");
    }

    @Test
    public void flatMapLeft_selfIsRight_returnRight() {
        Either<Integer, String> self = Either.right("right");

        Either<String, String> result = self.flatMapLeft(i -> Either.left(i.toString()));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("right");
    }

    @Test
    public void rightOrElse_selfIsRight_returnSelf() {
        Either<?, String> self = Either.right("right");

        Either<?, String> result = self.rightOrElse(Either.right("another right"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("right");
    }

    @Test
    public void rightOrElse_selfIsLeft_returnFallback() {
        Either<Integer, String> self = Either.left(1);

        Either<Integer, String> result = self.rightOrElse(Either.right("right"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("right");
    }

    @Test
    public void orElse_selfIsRight_returnSelf() {
        Either<?, String> self = Either.right("right");

        Either<?, String> result = self.orElse(Either.right("another right"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("right");
    }

    @Test
    public void orElse_selfIsLeft_returnFallback() {
        Either<Integer, String> self = Either.left(1);

        Either<Integer, String> result = self.orElse(Either.right("right"));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo("right");
    }

    @Test
    public void leftOrElse_selfIsRight_returnFallback() {
        Either<Integer, String> self = Either.right("right");

        Either<Integer, String> result = self.leftOrElse(Either.left(1));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(1);
    }

    @Test
    public void leftOrElse_selfIsLeft_returnSelf() {
        Either<Integer, String> self = Either.left(1);

        Either<Integer, String> result = self.leftOrElse(Either.right("right"));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(1);
    }

    @Test
    public void biMap_eitherIsLeft_mapLeftValue() {
        Either<Integer, String> either = Either.left(1);

        Either<Integer, Integer> result = either.biMap(i -> i + 1, String::length);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(2);
    }


    @Test
    public void biMap_eitherIsRight_mapRightValue() {
        Either<Integer, String> either = Either.right("hello");

        Either<Integer, Integer> result = either.biMap(i -> i + 1, String::length);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(5);
    }

    @Test
    public void biFlatMap_eitherIsLeftAndFunctionIsLeft_returnLeft() {
        Either<String, Integer> either = Either.left("error");

        Either<String, Integer> result = either.biFlatMap(e -> Either.left(e.toUpperCase()), Either::right);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("ERROR");
    }

    @Test
    public void biFlatMap_eitherIsLeftAndFunctionIsRight_returnRight() {
        Either<String, Integer> either = Either.left("error");

        Either<String, Integer> result = either.biFlatMap(e -> Either.right(e.length()), Either::right);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(5);
    }

    @Test
    public void biFlatMap_eitherIsRightAndFunctionIsRight_returnRight() {
        Either<String, Integer> either = Either.right(10);

        Either<String, Integer> result = either.biFlatMap(Either::left, v -> Either.right(v + 10));

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(20);
    }

    @Test
    public void biFlatMap_eitherIsRightAndFunctionIsLeft_returnLeft() {
        Either<String, Integer> either = Either.right(10);

        Either<String, Integer> result = either.biFlatMap(Either::left, v -> Either.left("" + v));

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("10");
    }

    @Test
    public void sequenceRight_allAreRight_returnRight() {
        List<Either<String, Integer>> list = List.of(Either.right(1), Either.right(2), Either.right(3));

        Either<String, List<Integer>> result = Either.rightSequence(list);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    public void sequenceRight_anyOneIsLeft_returnLeft() {
        List<Either<String, Integer>> list = List.of(Either.right(1), Either.left("error"), Either.right(3));

        Either<String, List<Integer>> result = Either.rightSequence(list);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void sequence_allAreRight_returnRight() {
        List<Either<String, Integer>> list = List.of(Either.right(1), Either.right(2), Either.right(3));

        Either<String, List<Integer>> result = Either.sequence(list);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(List.of(1, 2, 3));
    }

    @Test
    public void sequence_anyOneIsLeft_returnLeft() {
        List<Either<String, Integer>> list = List.of(Either.right(1), Either.left("error"), Either.right(3));

        Either<String, List<Integer>> result = Either.sequence(list);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void sequenceLeft_allAreLeft_returnLeft() {
        List<Either<String, Integer>> list = List.of(Either.left("1"), Either.left("2"), Either.left("3"));

        Either<List<String>, Integer> result = Either.leftSequence(list);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo(List.of("1", "2", "3"));
    }

    @Test
    public void sequenceLeft_anyOneIsRight_returnRight() {
        List<Either<String, Integer>> list = List.of(Either.left("1"), Either.left("2"), Either.right(3));

        Either<List<String>, Integer> result = Either.leftSequence(list);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(3);
    }

    @Test
    public void getRightOrElse_eitherIsRight_returnRightValue() {
        Either<String, Integer> either = Either.right(1);

        Integer result = either.getRightOrElse(String::length);

        assertThat(result).isEqualTo(1);
    }

    @Test
    public void getRightOrElse_eitherIsLeft_returnFallback() {
        Either<String, Integer> either = Either.left("hello");

        Integer result = either.getRightOrElse(String::length);

        assertThat(result).isEqualTo(5);
    }

    @Test
    public void getLeftOrElse_eitherIsRight_returnFallback() {
        Either<String, Integer> either = Either.right(1);

        String result = either.getLeftOrElse(Object::toString);

        assertThat(result).isEqualTo("1");
    }

    @Test
    public void getLeftOrElse_eitherIsLeft_returnLeftValue() {
        Either<String, Integer> either = Either.left("hello");

        String result = either.getLeftOrElse(Object::toString);

        assertThat(result).isEqualTo("hello");
    }

    @Test
    public void getLeftOrThrow_eitherIsLeft_returnLeft() {
        Either<String, Integer> either = Either.left("hello");

        String result = either.getLeftOrThrow(i -> new RuntimeException(i.toString()));

        assertThat(result).isEqualTo("hello");
    }

    @Test
    public void getLeftOrThrow_eitherIsRight_throwError() {
        Either<String, Integer> either = Either.right(1);

        Throwable error = catchThrowable(() -> either.getLeftOrThrow(i -> new RuntimeException(i.toString())));

        assertThat(error).isInstanceOf(RuntimeException.class)
                .hasMessage("1");
    }

    @Test
    public void getRightOrThrow_eitherIsRight_returnRightValue() {
        Either<String, Integer> either = Either.right(1);

        Integer result = either.getRightOrThrow(RuntimeException::new);

        assertThat(result).isEqualTo(1);
    }

    @Test
    public void getRightOrThrow_eitherIsLeft_throwError() {
        Either<String, Integer> either = Either.left("error");

        Throwable error = catchThrowable(() -> either.getRightOrThrow(RuntimeException::new));

        assertThat(error).isInstanceOf(RuntimeException.class)
                .hasMessage("error");
    }

    @Test
    public void flattenRight_outEitherIsLeft_returnLeft() {
        Either<String, Either<String, Integer>> either = Either.left("error");

        Either<String, Integer> result = Either.rightFlatten(either);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void flattenRight_innerEitherIsLeft_returnLeft() {
        Either<String, Either<String, Integer>> either = Either.right(Either.left("error"));

        Either<String, Integer> result = Either.rightFlatten(either);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void flattenRight_bothAreRight_returnRight() {
        Either<String, Either<String, Integer>> either = Either.right(Either.right(1));

        Either<String, Integer> result = Either.rightFlatten(either);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(1);
    }

    @Test
    public void flatten_outEitherIsLeft_returnLeft() {
        Either<String, Either<String, Integer>> either = Either.left("error");

        Either<String, Integer> result = Either.flatten(either);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void flatten_innerEitherIsLeft_returnLeft() {
        Either<String, Either<String, Integer>> either = Either.right(Either.left("error"));

        Either<String, Integer> result = Either.flatten(either);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }

    @Test
    public void flatten_bothAreRight_returnRight() {
        Either<String, Either<String, Integer>> either = Either.right(Either.right(1));

        Either<String, Integer> result = Either.flatten(either);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(1);
    }

    @Test
    public void flattenLeft_outIsRight_returnRight() {
        Either<Either<String, Integer>, Integer> either = Either.right(1);

        Either<String, Integer> result = Either.leftFlatten(either);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(1);
    }

    @Test
    public void flattenLeft_innerIsRight_returnRight() {
        Either<Either<String, Integer>, Integer> either = Either.left(Either.right(1));

        Either<String, Integer> result = Either.leftFlatten(either);

        assertThat(result.isRight()).isTrue();
        assertThat(result.getRight()).isEqualTo(1);
    }

    @Test
    public void flattenLeft_bothAreLeft_returnLeft() {
        Either<Either<String, Integer>, Integer> either = Either.left(Either.left("error"));

        Either<String, Integer> result = Either.leftFlatten(either);

        assertThat(result.isLeft()).isTrue();
        assertThat(result.getLeft()).isEqualTo("error");
    }
}