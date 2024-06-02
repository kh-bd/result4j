package dev.khbd.result4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.testng.annotations.Test;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Sergei Khadanovich
 */
public class ResultTest {

    @Test
    public void ap2_firstResultIsError_returnError() {
        Result<String, ?> result = Result.ap(Result.success(1), Result.<String, String>error("error"))
                .apply((r1, r2) -> r2 + r1);

        assertError(result, "error");
    }

    @Test
    public void ap2_secondResultIsError_returnError() {
        Result<String, String> result = Result.ap(Result.error("error"), Result.success("Alex"))
                .apply((r1, r2) -> r2 + r1);

        assertError(result, "error");
    }

    @Test
    public void ap2_bothResultsAreSuccess_returnCombinedResult() {
        Result<?, String> result = Result.ap(Result.success(1), Result.success("Alex"))
                .apply((r1, r2) -> r2 + r1);

        assertSuccess(result, "Alex1");
    }

    @Test
    public void isError_resultIsError_returnTrue() {
        Result<String, ?> result = Result.error("error");

        assertThat(result.isError()).isTrue();
        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    public void isSuccess_resultIsSuccess_returnTrue() {
        Result<?, Integer> result = Result.success(10);

        assertThat(result.isError()).isFalse();
        assertThat(result.isSuccess()).isTrue();
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Result is error")
    public void get_resultIsError_throwError() {
        Result.error("error").get();
    }

    @Test
    public void get_resultIsSuccess_returnValue() {
        Result<?, Integer> result = Result.success(10);

        assertThat(result.get()).isEqualTo(10);
    }

    @Test
    public void getError_resultIsError_returnError() {
        Result<String, ?> result = Result.error("error");

        assertThat(result.getError()).isEqualTo("error");
    }

    @Test(expectedExceptions = IllegalStateException.class, expectedExceptionsMessageRegExp = "Result is success")
    public void getError_resultIsSuccess_throwError() {
        Result.success(10).getError();
    }

    @Test
    public void map_resultIsError_doNothing() {
        Result<String, Integer> result = Result.error("error").map(__ -> 10);

        assertError(result, "error");
    }

    @Test
    public void map_resultIsSuccess_returnTransformedResult() {
        Result<String, Integer> result = Result.<String, Integer>success(1).map(n -> n + 1);


        assertSuccess(result, 2);
    }

    @Test
    public void mapError_resultIsError_returnTransformedResult() {
        Result<Integer, ?> result = Result.error("error").mapError(String::length);

        assertError(result, 5);
    }

    @Test
    public void mapError_resultIsSuccess_doNothing() {
        Result<Integer, Integer> result = Result.<String, Integer>success(10).mapError(String::length);

        assertSuccess(result, 10);
    }

    @Test
    public void filter_resultIsError_doNothing() {
        Result<String, ?> result = Result.error("error").filter(__ -> false, () -> "Ops");

        assertError(result, "error");
    }

    @Test
    public void filter_resultIsSuccessAndPredicateFailed_returnError() {
        var result = Result.<String, Integer>success(10).filter(n -> n == 10, () -> "Ops");

        assertSuccess(result, 10);
    }

    @Test
    public void filter_resultIsSuccessAndPredicateIsTrue_returnSuccess() {
        var result = Result.<String, Integer>success(10).filter(n -> n != 10, () -> "Ops");

        assertError(result, "Ops");
    }

    @Test
    public void flatMap_resultIsError_doNothing() {
        Result<String, Integer> result = Result.error("error");

        Result<String, ?> mapped = result.flatMap(__ -> Result.error("Ops"));

        assertError(mapped, "error");
    }

    @Test
    public void flatMap_resultIsSuccessAndReturnSuccess_returnSuccess() {
        Result<String, Integer> result = Result.success(10);

        Result<String, Integer> mapped = result.flatMap(n -> Result.success(n + 2));

        assertSuccess(mapped, 12);
    }

    @Test
    public void flatMap_resultIsSuccessAndReturnFailure_returnFailure() {
        Result<String, Integer> result = Result.success(10);

        Result<String, Integer> mapped = result.flatMap(n -> Result.error(n + " Ops"));

        assertError(mapped, "10 Ops");
    }

    @Test
    public void swap_resultIsError_returnSuccess() {
        Result<?, String> result = Result.error("error").swap();

        assertSuccess(result, "error");
    }

    @Test
    public void swap_resultIsSuccess_returnError() {
        Result<String, ?> result = Result.success("error").swap();

        assertError(result, "error");
    }

    @Test
    public void peek_resultIsSuccess_invokeFunction() {
        Result<String, Integer> result = Result.success(10);
        Consumer<Number> f = mock(Consumer.class);

        result.peek(f);

        verify(f, times(1)).accept(10);
    }

    @Test
    public void peek_resultIsError_ignoreInvocation() {
        Result<String, Integer> result = Result.error("error");
        Consumer<Number> f = mock(Consumer.class);

        result.peek(f);

        verify(f, never()).accept(any());
    }

    @Test
    public void peekError_resultIsSuccess_ignoreInvocation() {
        Result<String, Integer> result = Result.success(10);
        Consumer<CharSequence> f = mock(Consumer.class);

        result.peekError(f);

        verify(f, never()).accept(any());
    }

    @Test
    public void peekError_resultIsError_invokeFunction() {
        Result<String, Integer> result = Result.error("error");
        Consumer<String> f = mock(Consumer.class);

        result.peekError(f);

        verify(f, times(1)).accept("error");
    }

    @Test
    public void getOrElse_valueIsSuccess_returnThatValue() {
        Integer result = Result.success(10).getOrElse(12);

        assertThat(result).isEqualTo(10);
    }

    @Test
    public void getOrElse_valueIsFailure_returnDefaultValue() {
        Integer result = Result.<String, Integer>error("error").getOrElse(12);

        assertThat(result).isEqualTo(12);
    }

    @Test
    public void getOrElseF_valueIsSuccess_returnThatValue() {
        Function<String, Integer> elseF = mock(Function.class);
        when(elseF.apply(any())).thenReturn(12);

        Integer result = Result.<String, Integer>success(10).getOrElse(elseF);

        assertThat(result).isEqualTo(10);
        verify(elseF, never()).apply(anyString());
    }

    @Test
    public void getOrElseF_valueIsFailure_returnDefaultValue() {
        Integer result = Result.<String, Integer>error("error").getOrElse(__ -> 12);

        assertThat(result).isEqualTo(12);
    }

    @Test
    public void getOrThrow_valueIsSuccess_returnThatValue() {
        Result<String, Integer> result = Result.success(10);

        Integer value = result.getOrThrow(__ -> new RuntimeException("Ops"));

        assertThat(value).isEqualTo(10);
    }

    @Test(expectedExceptions = RuntimeException.class, expectedExceptionsMessageRegExp = "Ops")
    public void getOrThrow_valueIsFailure_throwException() {
        Result.error("error").getOrThrow(__ -> new RuntimeException("Ops"));
    }

    @Test
    public void orElse_resultIsError_returnDefault() {
        Result<String, Integer> result = Result.<String, Integer>error("error")
                .orElse(Result.success(10));

        assertSuccess(result, 10);
    }

    @Test
    public void orElse_resultIsSuccess_returnSelf() {
        Result<String, Integer> result = Result.<String, Integer>success(9)
                .orElse(Result.success(10));

        assertSuccess(result, 9);
    }

    @Test
    public void bimap_resultIsError_mapError() {
        Function<String, Integer> errorF = mock(Function.class);
        Function<Integer, String> successF = mock(Function.class);
        Result<String, Integer> result = Result.error("error");
        when(errorF.apply(anyString())).thenReturn(5);

        Result<Integer, String> bimaped = result.bimap(String::length, successF);

        assertError(bimaped, 5);
        verify(successF, never()).apply(any());
    }

    @Test
    public void bimap_resultIsSuccess_mapSuccess() {
        Function<String, Integer> errorF = mock(Function.class);
        Function<Integer, String> successF = mock(Function.class);
        Result<String, Integer> result = Result.success(10);
        when(successF.apply(any())).thenReturn("10");

        Result<Integer, String> bimaped = result.bimap(String::length, successF);

        assertSuccess(bimaped, "10");
        verify(errorF, never()).apply(any());
    }

    @Test
    public void flatten_resultIsError_returnIt() {
        Result<String, ?> result = Result.flatten(Result.error("error"));

        assertError(result, "error");
    }

    @Test
    public void flatten_resultIsSuccessWithError_returnNestedError() {
        Result<String, ?> result = Result.flatten(Result.success(Result.error("error")));

        assertError(result, "error");
    }

    @Test
    public void flatten_resultIsSuccessWithSuccess_returnSuccess() {
        Result<String, Integer> result = Result.flatten(Result.success(Result.success(10)));

        assertSuccess(result, 10);
    }

    @Test
    public void drop_valueIsError_doNothing() {
        Result<Integer, NoData> result = Result.error(10).drop();

        assertError(result, 10);
    }

    @Test
    public void drop_valueIsSuccess_dropSuccessValue() {
        Result<?, NoData> result = Result.success(10).drop();

        assertSuccess(result, NoData.INSTANCE);
    }

    @Test
    public void dropError_valueIsError_dropIt() {
        Result<NoData, ?> result = Result.error("error").dropError();

        assertError(result, NoData.INSTANCE);
    }

    @Test
    public void dropError_valueIsSuccess_doNothing() {
        Result<NoData, String> result = Result.success("text").dropError();

        assertSuccess(result, "text");
    }

    @Test
    public void toOption_valueIsError_returnNone() {
        Option<?> result = Result.error("error").toOption();

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void toOption_valueIsSuccess_returnSome() {
        Option<Integer> result = Result.success(10).toOption();

        assertThat(result.isEmpty()).isFalse();
        assertThat(result.get()).isEqualTo(10);
    }

    @Test
    public void toStream_valueIsError_returnEmptyStream() {
        Stream<Integer> result = Result.<String, Integer>error("error").toStream();

        assertThat(result.collect(Collectors.toList())).isEmpty();
    }

    @Test
    public void toStream_valueIsSuccess_returnOneElementStream() {
        Stream<Integer> result = Result.success(10).toStream();

        assertThat(result.collect(Collectors.toList())).containsExactly(10);
    }

    @Test
    public void fromOption_optionIsNone_returnError() {
        Result<String, Integer> result = Result.fromOption(Option.none(), "error");

        assertError(result, "error");
    }

    @Test
    public void fromOption_optionIsSome_returnSuccess() {
        Result<String, Integer> result = Result.fromOption(Option.some(10), "error");

        assertSuccess(result, 10);
    }

    @Test
    public void fromOptional_optionalIsEmpty_returnError() {
        Result<String, Integer> result = Result.fromOptional(Optional.empty(), "error");

        assertError(result, "error");
    }

    @Test
    public void fromOptional_optionalIsNotEmpty_returnSuccess() {
        Result<String, Integer> result = Result.fromOptional(Optional.of(10), "error");

        assertSuccess(result, 10);
    }

    @Test
    public void fromNullable_valueIsNull_returnError() {
        Result<String, Integer> result = Result.fromNullable(null, "error");

        assertError(result, "error");
    }

    @Test
    public void fromNullable_valueIsNotNull_returnSuccess() {
        Result<String, Integer> result = Result.fromNullable(10, "error");

        assertSuccess(result, 10);
    }

    @Test
    public void sequencing_allValuesAreSuccess_returnCollectionOfThem() {
        Result<String, List<Integer>> result =
                Stream.<Result<String, Integer>>of(Result.success(10), Result.success(20), Result.success(30), Result.success(40))
                        .collect(Result.sequencing(Collectors.toList()));

        assertSuccess(result, List.of(10, 20, 30, 40));
    }

    @Test
    public void sequencing_atLeastOneItemIsError_returnError() {
        Result<String, List<Integer>> result =
                Stream.<Result<String, Integer>>of(Result.success(10), Result.error("error"), Result.success(20))
                        .collect(Result.sequencing(Collectors.toList()));

        assertError(result, "error");
    }

    @Test
    public void traversing_allValuesAreSuccess_returnCollectionOfThem() {
        Result<String, Set<Integer>> result = Stream.of(10, 20, 30, 40)
                .collect(Result.traversing(Result::<String, Integer>success, Collectors.toSet()));

        assertSuccess(result, Set.of(10, 20, 30, 40));
    }

    @Test
    public void traversing_someValuesAreError_returnError() {
        Result<String, Set<Integer>> result = Stream.of(10, 20, 30, 40)
                .collect(Result.traversing(num -> {
                    if (num == 20) {
                        return Result.error("error");
                    }
                    return Result.success(num);
                }, Collectors.toSet()));

        assertError(result, "error");
    }

    @Test
    public void fromErroneousRunnable_withoutError_returnSuccess() {
        Result<Exception, NoData> result = Result.fromErroneous(() -> {
            System.out.println("10");
        });

        assertSuccess(result, NoData.INSTANCE);
    }

    @Test
    public void fromErroneousRunnable_withError_returnFailure() {
        Runnable code = () -> {
            throw new RuntimeException("error");
        };
        Result<Exception, NoData> result = Result.fromErroneous(code);

        assertThat(result.isError()).isTrue();
        assertThat(result.getError()).hasMessage("error").isInstanceOf(RuntimeException.class);
    }

    @Test
    public void fromErroneousCallable_withoutError_returnSuccess() {
        Result<Exception, Integer> result = Result.fromErroneous(() -> {
            System.out.println("10");
            return 10;
        });

        assertSuccess(result, 10);
    }

    @Test
    public void fromErroneousCallable_withError_returnFailure() {
        Callable<Integer> code = () -> {
            throw new RuntimeException("error");
        };
        Result<Exception, Integer> result = Result.fromErroneous(code);

        assertThat(result.isError()).isTrue();
        assertThat(result.getError()).hasMessage("error").isInstanceOf(RuntimeException.class);
    }

    private static <E, V> void assertError(Result<E, V> result, E expected) {
        assertThat(result.isError()).isTrue();
        assertThat(result.getError()).isEqualTo(expected);
    }

    private static <E, V> void assertSuccess(Result<E, V> result, V expected) {
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(expected);
    }
}