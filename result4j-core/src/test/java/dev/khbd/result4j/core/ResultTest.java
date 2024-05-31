package dev.khbd.result4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.testng.annotations.Test;

import java.util.function.Consumer;

/**
 * @author Sergei Khadanovich
 */
public class ResultTest {

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

    private static <E, V> void assertError(Result<E, V> result, E expected) {
        assertThat(result.isError()).isTrue();
        assertThat(result.getError()).isEqualTo(expected);
    }

    private static <E, V> void assertSuccess(Result<E, V> result, V expected) {
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(expected);
    }
}