package dev.khbd.result4j.core;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.testng.annotations.Test;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * @author Sergei Khadanovich
 */
public class TryTest {

    @Test
    public void orElse_valueIsSuccess_returnIt() {
        Try<String> value = Try.success("hello").orElse(Throwable::getMessage);

        assertThat(value.isSuccess()).isTrue();
        assertThat(value.get()).isEqualTo("hello");
    }

    @Test
    public void orElse_valueIsFailure_returnRecovered() {
        Try<String> value = Try.<String>failure(new RuntimeException("Ops"))
                .orElse(Throwable::getMessage);

        assertThat(value.isSuccess()).isTrue();
        assertThat(value.get()).isEqualTo("Ops");
    }

    @Test
    public void orElseF_valueIsSuccess_returnIt() {
        Try<String> value = Try.success("hello").orElseF(error -> Try.success(error.getMessage()));

        assertThat(value.isSuccess()).isTrue();
        assertThat(value.get()).isEqualTo("hello");
    }

    @Test
    public void orElseF_valueIsFailureAndRecoverIsSuccess_returnRecovered() {
        Try<String> value = Try.<String>failure(new RuntimeException("Ops"))
                .orElseF(error -> Try.success(error.getMessage()));

        assertThat(value.isSuccess()).isTrue();
        assertThat(value.get()).isEqualTo("Ops");
    }

    @Test
    public void orElseF_valueIsFailureAndRecoverIsFailure_returnRecovered() {
        Try<String> value = Try.<String>failure(new RuntimeException("Ops"))
                .orElseF(error -> Try.failure(new RuntimeException("Recovered")));

        assertThat(value.isSuccess()).isFalse();
        assertThat(value.getError().getMessage()).isEqualTo("Recovered");
    }

    @Test
    public void ifBoth_valueIsSuccess_invokeSuccessCallback() {
        Consumer<String> successF = mock(Consumer.class);
        Consumer<Throwable> failureF = mock(Consumer.class);

        Try.success("hello")
                .ifBoth(successF, failureF);

        verify(successF, times(1)).accept("hello");
        verify(failureF, never()).accept(any());
    }

    @Test
    public void ifBoth_valueIsFailure_invokeFailureCallback() {
        Consumer<String> successF = mock(Consumer.class);
        Consumer<Throwable> failureF = mock(Consumer.class);

        Try.<String>failure(new RuntimeException())
                .ifBoth(successF, failureF);

        verify(successF, never()).accept(anyString());
        verify(failureF, times(1)).accept(any());
    }

    @Test
    public void ifFailure_valueIsSuccess_notInvokeCallback() {
        Consumer<Throwable> mock = mock(Consumer.class);

        Try.success("Hello")
                .ifFailure(mock);

        verify(mock, never()).accept(any());
    }

    @Test
    public void ifFailure_valueIsFailure_invokeCallback() {
        Consumer<Throwable> mock = mock(Consumer.class);

        Try.failure(new RuntimeException("Ops"))
                .ifFailure(mock);

        verify(mock, times(1)).accept(any());
    }

    @Test
    public void ifSuccess_valueIsSuccess_invokeCallback() {
        Consumer<String> mock = mock(Consumer.class);

        Try.success("hello")
                .ifSuccess(mock);

        verify(mock, times(1)).accept("hello");
    }

    @Test
    public void ifSuccess_valueIsFailure_notInvokeCallback() {
        Consumer<String> mock = mock(Consumer.class);

        Try.<String>failure(new RuntimeException("Ops"))
                .ifSuccess(mock);

        verify(mock, never()).accept(anyString());
    }

    @Test
    public void successNullable_valueIsNull_returnSuccess() {
        Try<?> result = Try.successNullable(null);

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void successNullable_valueIsNotNull_returnSuccess() {
        Try<Integer> result = Try.successNullable(1);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(1);
    }

    @Test
    public void toStream_valueIsFailure_returnEmpty() {
        Stream<Integer> result = Try.success(1).toStream();

        assertThat(result).containsExactlyInAnyOrder(1);
    }

    @Test
    public void toStream_valueIsSuccess_returnOneElementStream() {
        Stream<Integer> result = Try.<Integer>failure(new RuntimeException("ops")).toStream();

        assertThat(result).isEmpty();
    }

    @Test
    public void toOption_valueIsFailure_returnEmpty() {
        Option<?> result = Try.failure(new RuntimeException("Ops")).toOption();

        assertThat(result.isEmpty()).isTrue();
    }

    @Test
    public void toOption_valueIsSuccess_returnNotEmpty() {
        Option<String> result = Try.success("hello").toOption();

        assertThat(result.get()).isEqualTo("hello");
    }

    @Test
    public void ofNullable_codeReturnNull_returnSuccess() {
        Try<Object> result = Try.ofNullable(() -> null);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isNull();
    }

    @Test
    public void ofNullable_codeReturnValue_returnSuccess() {
        Try<String> result = Try.ofNullable(() -> "value");

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo("value");
    }

    @Test
    public void ofNullable_codeThrowNotFatalError_errorWasCaught() {
        RuntimeException error = new RuntimeException("error");

        Try<String> tr = Try.ofNullable(() -> {
            throw error;
        });

        assertThat(tr.isFailure()).isTrue();
        assertThat(tr.getError()).isEqualTo(error);
    }

    @Test
    public void ofNullable_codeThrowFatalError_propagateError() {
        Throwable error = catchThrowable(() -> Try.ofNullable(() -> {
            throw new OutOfMemoryError("error");
        }));

        assertThat(error).isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    public void of_codeReturnNull_returnFailure() {
        Try<Object> result = Try.of(() -> null);

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    public void of_codeReturnValue_valueWasCaught() {
        Try<String> tr = Try.of(() -> "hello");

        assertThat(tr.get()).isEqualTo("hello");
    }

    @Test
    public void of_codeThrowNotFatalError_errorWasCaught() {
        RuntimeException error = new RuntimeException("error");

        Try<String> tr = Try.of(() -> {
            throw error;
        });

        assertThat(tr.isFailure()).isTrue();
        assertThat(tr.getError()).isEqualTo(error);
    }

    @Test
    public void of_codeThrowFatalError_propagateError() {
        var error = catchThrowable(() -> Try.of(() -> {
            throw new OutOfMemoryError("error");
        }));

        assertThat(error).isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    public void success_valueIsNotNull_catchValue() {
        Try<String> hello = Try.success("hello");

        assertThat(hello.get()).isEqualTo("hello");
    }

    @Test
    public void failure_exceptionIsNull_throwNPE() {
        Throwable error = catchThrowable(() -> Try.failure(null));

        assertThat(error).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void failure_exceptionIsNotNull_catchException() {
        Exception error = new Exception("");

        Try<String> result = Try.failure(error);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void fromOption_valueIsNullAndThrowableSupplierExists_throwNPE() {
        Throwable error = catchThrowable(() -> Try.fromOption(null));

        assertThat(error).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void fromOption_valueIsEmptyAndThrowableSupplierExists_returnErroneousTry() {
        Try<?> result = Try.fromOption(Option.none(), RuntimeException::new);

        Throwable error = catchThrowable(result::get);

        assertThat(error).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void fromOption_valueIsNotEmptyAndThrowableSupplierExists_returnTryWithValue() {
        String result = Try.fromOption(Option.some("value"), () -> new RuntimeException("")).get();

        assertThat(result).isEqualTo("value");
    }

    @Test
    public void fromOption_valueIsNull_throwNPE() {
        Throwable error = catchThrowable(() -> Try.fromOption(null));

        assertThat(error).isInstanceOf(NullPointerException.class);
    }

    @Test
    public void fromOption_valueIsEmpty_returnErroneousTry() {
        Try<?> result = Try.fromOption(Option.none());

        Throwable error = catchThrowable(result::get);

        assertThat(error).isInstanceOf(RuntimeException.class);
    }

    @Test
    public void fromOption_valueIsNotEmpty_returnTryWithValue() {
        String result = Try.fromOption(Option.some("value")).get();

        assertThat(result).isEqualTo("value");
    }

    @Test
    public void map_valueIsNotPresent_skipMappingExecution() {
        Exception e = new Exception("");
        Try<String> error = Try.failure(e);
        Function<String, String> mapper = mock(Function.class);

        Try<String> result = error.map(mapper);

        verify(mapper, never()).apply(anyString());
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(e);
    }

    @Test
    public void map_valueIsPresent_returnMappedValue() {
        Try<String> tValue = Try.success("hello");

        Try<String> result = tValue.map(s -> s + s);

        assertThat(result.get()).isEqualTo("hellohello");
    }

    @Test
    public void map_valueIsPresentButMapFunctionThrowError_returnErroneousTry() {
        Try<String> tValue = Try.success("hello");
        RuntimeException error = new RuntimeException("bad string");

        Try<String> result = tValue.map(s -> {
            throw error;
        });

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void flatMap_valueIsNotPresent_skipMappingExecution() {
        Exception e = new Exception("");
        Try<String> error = Try.failure(e);
        Function<String, Try<Integer>> mapper = mock(Function.class);

        Try<Integer> result = error.flatMap(mapper);

        verify(mapper, never()).apply(anyString());
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(e);
    }

    @Test
    public void flatMap_valueIsPresent_returnFlatMappedValue() {
        Try<String> tValue = Try.success("hello");

        Try<Integer> result = tValue.flatMap(s -> Try.success(s.length()));

        assertThat(result.get()).isEqualTo(5);
    }

    @Test
    public void flatMap_valueIsPresentButMapFunctionThrowNotFatalError_returnErroneousTry() {
        Try<String> tValue = Try.success("hello");
        RuntimeException error = new RuntimeException("bad string");

        Try<String> result = tValue.flatMap(s -> {
            throw error;
        });

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void flatMap_valueIsPresentButMapFunctionThrowFatalError_returnErroneousTry() {
        Try<String> tValue = Try.success("hello");

        var error = catchThrowable(() -> tValue.flatMap(s -> {
            throw new OutOfMemoryError("error");
        }));

        assertThat(error).isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    public void filter_valueIsNotPresent_skipFiltering() {
        Exception e = new Exception("");
        Try<String> error = Try.failure(e);
        Predicate<String> predicate = mock(Predicate.class);

        Try<String> result = error.filter(predicate);

        verify(predicate, never()).test(anyString());
        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(e);
    }

    @Test
    public void filter_valueIsPresentButPredicateIsNotSatisfied_returnErroneousTry() {
        Try<String> tValue = Try.success("hello");

        Try<String> result = tValue.filter(s -> s.length() > 100);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError().getMessage()).isEqualTo("Value is not correspond specified predicate");
    }

    @Test
    public void filter_valueIsPresentButPredicateThrowNotFatalError_propagateThatError() {
        Try<String> value = Try.success("hello");
        RuntimeException error = new RuntimeException("error");

        Try<String> result = value.filter(s -> {
            throw error;
        });

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void filter_valueIsPresentButPredicateThrowFatalError_propagateThatError() {
        Try<String> value = Try.success("hello");

        Throwable error = catchThrowable(() -> value.filter(s -> {
            throw new OutOfMemoryError("error");
        }));

        assertThat(error).isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    public void filter_valueIsPresentAndPredicateIsSatisfied_returnTryWithValue() {
        Try<String> value = Try.success("hello");

        Try<String> result = value.filter(s -> s.length() == 5);

        assertThat(result.get()).isEqualTo("hello");
    }

    @Test
    public void zip_firstTryIsErroneous_returnThem() {
        Exception error = new Exception("");
        Try<String> first = Try.failure(error);
        Try<String> second = Try.success("hello");

        Try<String> result = first.zip(second, String::concat);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void zip_secondTryIsErroneous_returnThem() {
        Throwable error = new RuntimeException("error");
        Try<String> first = Try.success("hello");
        Try<String> second = Try.failure(error);

        Try<String> result = first.zip(second, String::concat);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void zip_bothContainsValue_returnCombinedValue() {
        Try<String> first = Try.success("hello");
        Try<String> second = Try.success(" world");

        Try<String> result = first.zip(second, String::concat);

        assertThat(result.get()).isEqualTo("hello world");
    }

    @Test
    public void zipF_bothAreSuccessAndFunctionReturnSuccess_returnSuccess() {
        Try<String> first = Try.success("hello");
        Try<String> second = Try.success(" world");

        Try<String> result = first.zipF(second, (v1, v2) -> Try.success(v1 + v2));

        assertThat(result.get()).isEqualTo("hello world");
    }

    @Test
    public void zipF_bothAreSuccessAndFunctionReturnFail_returnFail() {
        Try<String> first = Try.success("hello");
        Try<String> second = Try.success(" world");
        Exception error = new Exception("error");

        Try<String> result = first.zipF(second, (v1, v2) -> Try.failure(error));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void zipF_firstIsFail_returnFail() {
        Exception error = new Exception("error");
        Try<String> first = Try.failure(error);
        Try<String> second = Try.success(" world");

        Try<String> result = first.zipF(second, (v1, v2) -> Try.success(v1 + v2));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void zipF_secondIsFail_returnFail() {
        Exception error = new Exception("error");
        Try<String> first = Try.success("hello");
        Try<String> second = Try.failure(error);

        Try<String> result = first.zipF(second, (v1, v2) -> Try.success(v1 + v2));

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void isError_tryIsErroneous_returnTrue() {
        Boolean result = Try.failure(new Exception()).isFailure();

        assertThat(result).isTrue();
    }

    @Test
    public void isError_tryIsNotErroneous_returnFalse() {
        Boolean result = Try.success("hello").isFailure();

        assertThat(result).isFalse();
    }

    @Test
    public void getError_tryIsErroneous_returnError() {
        Exception error = new Exception();
        Throwable result = Try.failure(error).getError();

        assertThat(result).isEqualTo(error);
    }

    @Test
    public void getError_tryIsNotErroneous_throwError() {
        Throwable error = catchThrowable(() -> Try.success("hello").getError());

        assertThat(error).isInstanceOf(RuntimeException.class)
                .hasMessage("Try is not erroneous");
    }

    @Test
    public void getOrElse_tryIsNotErroneous_returnValue() {
        String result = Try.success("someValue").getOrElse(ex -> "newValue");

        assertThat(result).isEqualTo("someValue");
    }

    @Test
    public void getOrElse_tryIsErroneous_returnValue() {
        String result = Try.of(() -> {
            throw new RuntimeException("message");
        }).map(String::valueOf).getOrElse(Throwable::getMessage);

        assertThat(result).isEqualTo("message");
    }

    @Test
    public void getOrElseSupplier_tryIsNotErroneous_returnValue() {
        String result = Try.success("someValue").getOrElse(() -> "newValue");

        assertThat(result).isEqualTo("someValue");
    }

    @Test
    public void getOrElseSupplier_tryIsErroneous_returnValue() {
        String result = Try.of(() -> {
                    throw new RuntimeException("message");
                }).map(String::valueOf)
                .getOrElse(() -> "message from supplier");

        assertThat(result).isEqualTo("message from supplier");
    }

    @Test
    public void getOrElseThrow_tryIsNotErroneous_returnValue() {
        String result = Try.success("someValue").getOrElseThrow(it -> new RuntimeException());

        assertThat(result).isEqualTo("someValue");
    }

    @Test
    public void getOrElseThrow_tryIsErroneous_returnValue() {
        Try<Object> tr = Try.of(() -> {
            throw new RuntimeException("");
        });

        Throwable error = catchThrowable(() -> tr.getOrElseThrow(it -> new RuntimeException("Some error")));

        assertThat(error).isInstanceOf(RuntimeException.class)
                .hasMessage("Some error");
    }

    @Test
    public void sequence_someSuccessListOfTry_returnTryOfList() {
        Try<List<Integer>> result = Try.sequence(List.of(Try.success(1), Try.success(2)));

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).containsOnly(1, 2);
    }

    @Test
    public void sequence_oneFailureTryAndOtherIsSuccess_returnTryOfList() {
        Try<List<Integer>> result = Try.sequence(List.of(Try.success(1), Try.failure(new RuntimeException())));

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    public void traverse_someSuccessListOfTry_returnTryOfList() {
        Try<List<Integer>> result = Try.traverse(
                List.of(1, 2),
                it -> Try.success(it + 1)
        );

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).containsOnly(2, 3);
    }

    @Test
    public void traverse_oneFailureTryAndOtherIsSuccess_returnTryOfList() {
        Try<List<Integer>> result = Try.traverse(
                List.of(1, 2),
                it -> Try.of(() -> it / 0)
        );

        assertThat(result.isFailure()).isTrue();
    }

    @Test
    public void traverse_emptyListOfTry_returnTryOfList() {
        Try<List<Integer>> result = Try.traverse(
                List.of(),
                Function.identity()
        );

        assertThat(result.isSuccess()).isTrue();
    }

    @Test
    public void traverse_functionThrowError_returnFailure() {
        RuntimeException error = new RuntimeException("error");
        Try<List<Integer>> result = Try.traverse(
                List.of(1, 2),
                it -> {
                    throw error;
                }
        );

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void flatten_tryIsFailure_returnSelf() {
        Throwable error = new RuntimeException("");
        Try<Try<Integer>> tr = Try.failure(error);

        Try<Integer> result = Try.flatten(tr);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void flatten_tryIsSuccessButNestedIsFailure_returnFailure() {
        Throwable error = new RuntimeException("");
        Try<Try<Integer>> tr = Try.success(Try.failure(error));

        Try<Integer> result = Try.flatten(tr);

        assertThat(result.isFailure()).isTrue();
        assertThat(result.getError()).isEqualTo(error);
    }

    @Test
    public void flatten_tryIsSuccessAndNestedIsSuccess_returnSuccess() {
        Try<Try<Integer>> tr = Try.success(Try.success(1));

        Try<Integer> result = Try.flatten(tr);

        assertThat(result.isSuccess()).isTrue();
        assertThat(result.get()).isEqualTo(1);
    }

    @Test
    public void fromEither_eitherIsLeft_returnFailure() {
        Throwable e = new RuntimeException("error");
        Either<Throwable, ?> either = Either.left(e);

        Try<?> tr = Try.fromEither(either);

        assertThat(tr.isFailure()).isTrue();
        assertThat(tr.getError()).isEqualTo(e);
    }

    @Test
    public void fromEither_eitherIsRight_returnSuccess() {
        Either<RuntimeException, Integer> either = Either.right(1);

        Try<Integer> tr = Try.fromEither(either);

        assertThat(tr.isSuccess()).isTrue();
        assertThat(tr.get()).isEqualTo(1);
    }
}