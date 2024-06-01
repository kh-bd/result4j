package dev.khbd.result4j.core;

import static dev.khbd.result4j.core.Utils.cast;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Result data type.
 *
 * @param <E> error value type
 * @param <R> success value type
 * @author Sergei Khadanovich
 */
public interface Result<E, R> {

    /**
     * Is result error.
     *
     * @return {@literal true} if result is error and {@literal false} otherwise
     */
    boolean isError();

    /**
     * Is result success.
     *
     * @return {@literal true} if result is success and {@literal false} otherwise
     */
    default boolean isSuccess() {
        return !isError();
    }

    /**
     * Get error value.
     *
     * @return error value
     * @throws IllegalStateException if result is success
     */
    E getError();

    /**
     * Get success value.
     *
     * @return success value
     * @throws IllegalStateException if result is error
     */
    R get();

    /**
     * Get success value or default
     *
     * @param other default value
     * @return success value
     */
    R getOrElse(R other);

    /**
     * Get success value or default.
     *
     * @param other default value
     * @return success value
     */
    R getOrElse(Function<? super E, ? extends R> other);

    /**
     * Get success value or throw error.
     *
     * @param errorF error function
     * @param <T>    exception type
     * @return right value
     */
    <T extends Throwable> R getOrThrow(Function<? super E, T> errorF) throws T;

    /**
     * Transform success value.
     *
     * @param f   transform function
     * @param <V> new success value type
     * @return transformed result
     */
    <V> Result<E, V> map(Function<? super R, V> f);

    /**
     * Transform success value into new result.
     *
     * @param f   transform function
     * @param <V> new success value type
     * @return transformed result
     */
    <V> Result<E, V> flatMap(Function<? super R, Result<? extends E, V>> f);

    /**
     * Transform error value.
     *
     * @param f   transform function
     * @param <O> new error type
     * @return transformed result
     */
    <O> Result<O, R> mapError(Function<? super E, O> f);

    /**
     * Transformed error value if result is error and transform success value if result is success.
     *
     * @param errorF   error transform function
     * @param successF success transform function
     * @param <O>      new error type
     * @param <V>      new success type
     * @return transformed result
     */
    <O, V> Result<O, V> bimap(Function<? super E, O> errorF, Function<? super R, V> successF);

    /**
     * Check success value with predicate.
     *
     * @param predicate predicate to check value
     * @param errorF    error provider
     * @return transformed result value
     */
    Result<E, R> filter(Predicate<? super R> predicate, Supplier<? extends E> errorF);

    /**
     * Execute function if result is success.
     *
     * @param f function
     * @return result value
     */
    Result<E, R> peek(Consumer<? super R> f);

    /**
     * Execute function if result is error.
     *
     * @param f function
     * @return result
     */
    Result<E, R> peekError(Consumer<? super E> f);

    /**
     * Unwrap call.
     *
     * <p>This is method with special support through compiler plugin.
     * <p>Invocation of this method is replaced with special statements at compile time.
     * <p>For example, we have a function which is able to divide integers
     * <pre>{@code
     *     Result<String, Integer> divide(Integer num, Integer den) {
     *         if (den == 0) {
     *              return Result.error("Division by zero");
     *         }
     *         return Result.success(num / den);
     *     }
     * }</pre>
     * and we need to write a function which sums results of two divisions.
     * Such function can be written with zip combinator.
     * <pre>{@code
     *     Result<String, Integer> sumDivide(Integer num1, Integer den1, Integer num2, Integer den2) {
     *          Result<String, Integer> r1 = divide(num1, den1);
     *          Result<String, Integer> r2 = divide(num2, den2);
     *          return Result.zip(r1, r2, (v1, v2) -> v1 + v2);
     *     }
     * }</pre>
     * This example is very simple but a bit complicated to read.
     * The same code be written like this
     * <pre>{@code
     *     Result<String, Integer> sumDivide(Integer num1, Integer den1, Integer num2, Integer den2) {
     *         Integer r1 = divide(num1, den1).unwrap();
     *         Integer r2 = divide(num2, den2).unwrap();
     *         return Result.success(r1 + r2);
     *     }
     * }</pre>
     *
     * <p>Unwrap call is transformed at compile time. Simplified code looks like this.
     * <pre>{@code
     *      // invocation like this
     *      Integer result = divide(num, den).unwrap();
     *
     *      // is going to be transformed into several statements
     *      Result<String, Integer> $$rev = divide(num, den);
     *      if ($$rev.isError()) {
     *          return Result.error($$rev.getError());
     *      }
     *      Integer result = $$rev.get();
     * }</pre>
     */
    default R unwrap() {
        throw new UnsupportedOperationException("This is a method with special support at compile time");
    }

    /**
     * Recover result instance.
     *
     * @param other result recovering result instance
     */
    default Result<E, R> orElse(@NonNull Result<? extends E, ? extends R> other) {
        return orElse(__ -> other);
    }

    /**
     * Recover result instance.
     *
     * @param otherF recovering result instance factory
     */
    Result<E, R> orElse(Function<? super E, Result<? extends E, ? extends R>> otherF);

    /**
     * Swap result values.
     *
     * @return transformed result
     */
    Result<R, E> swap();

    /**
     * Drop success value.
     *
     * @return transformed result
     */
    default Result<E, NoData> drop() {
        return map(__ -> NoData.INSTANCE);
    }

    /**
     * Drop error value.
     *
     * @return transformed result
     */
    default Result<NoData, R> dropError() {
        return mapError(__ -> NoData.INSTANCE);
    }

    /**
     * Convert result to option value.
     *
     * @return option
     */
    default Option<R> toOption() {
        return map(Option::some).getOrElse(Option.none());
    }

    /**
     * Convert result to stream.
     *
     * @return stream
     */
    default Stream<R> toStream() {
        return map(Stream::of).getOrElse(Stream.empty());
    }

    /**
     * Create success result value.
     *
     * @param value success value
     * @param <E>   error type
     * @param <R>   success type
     * @return success result
     */
    static <E, R> Result<E, R> success(@NonNull R value) {
        return new Ok<>(value);
    }

    /**
     * Create failure result value.
     *
     * @param error error
     * @param <E>   error type
     * @param <R>   success type
     * @return error result
     */
    static <E, R> Result<E, R> error(@NonNull E error) {
        return new Error<>(error);
    }

    /**
     * Factory method to create result from option.
     *
     * @param option option value
     * @param error  error value
     * @param <E>    error value type
     * @param <R>    success value type
     * @return result
     */
    static <E, R> Result<E, R> fromOption(@NonNull Option<R> option, @NonNull E error) {
        return fromOption(option, () -> error);
    }

    /**
     * Factory method to create result from option element.
     *
     * @param option option value
     * @param errorF error value provider
     * @param <E>    error value type
     * @param <R>    success value type
     * @return result
     */
    static <E, R> Result<E, R> fromOption(@NonNull Option<R> option, @NonNull Supplier<E> errorF) {
        return option.map(Result::<E, R>success)
                .getOrElse(() -> Result.error(errorF.get()));
    }

    /**
     * Factory method to create result from value which can be {@literal null}.
     *
     * @param value possible nullable value
     * @param error error value
     * @param <E>   error value type
     * @param <R>   success value type
     * @return result
     */
    static <E, R> Result<E, R> fromNullable(R value, @NonNull E error) {
        return fromNullable(value, () -> error);
    }

    /**
     * Factory method to create result from value which can be {@literal null}.
     *
     * @param value  possible nullable value
     * @param errorF error factory
     * @param <E>    error value type
     * @param <R>    success value type
     * @return result
     */
    static <E, R> Result<E, R> fromNullable(R value, @NonNull Supplier<E> errorF) {
        return Objects.isNull(value) ? Result.error(errorF.get()) : Result.success(value);
    }

    /**
     * Create result from erroneous code.
     *
     * @param code code to run
     * @return result
     */
    static Result<Exception, NoData> fromErroneous(@NonNull Runnable code) {
        try {
            code.run();
            return Result.success(NoData.INSTANCE);
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    /**
     * Create result from erroneous code.
     *
     * @param code code to run
     * @param <R>  result success type
     * @return result
     */
    static <R> Result<Exception, R> fromErroneous(@NonNull Callable<R> code) {
        try {
            return Result.success(code.call());
        } catch (Exception e) {
            return Result.error(e);
        }
    }

    /**
     * Create sequence collector.
     *
     * @param <E> result error type
     * @param <R> result value type
     * @param <A> downstream accumulator type
     * @param <U> downstream result type
     * @return sequence collector
     */
    static <E, R, A, U> Collector<Result<E, R>, ?, Result<E, U>> sequencing(@NonNull Collector<? super R, A, U> downstream) {
        Supplier<A> supplier = downstream.supplier();
        BiConsumer<A, ? super R> accumulator = downstream.accumulator();
        BinaryOperator<A> combiner = downstream.combiner();
        Function<A, U> finisher = downstream.finisher();

        return Collector.<Result<E, R>, Ref<Result<E, A>>, Result<E, U>>of(
                () -> new Ref<>(Result.success(supplier.get())),
                (ref, result) -> {
                    ref.ref = Result.ap(ref.ref, result).apply((acc, r) -> {
                        accumulator.accept(acc, r);
                        return acc;
                    });
                },
                (ref1, ref2) -> new Ref<>(Result.ap(ref1.ref, ref2.ref).apply(combiner)),
                ref -> ref.ref.map(finisher)
        );
    }

    /**
     * Create traverse collector.
     *
     * @param <E> original stream element type
     * @param <L> result error type
     * @param <R> result success type
     * @param <A> downstream accumulator type
     * @param <U> downstream result type
     * @return sequence collector
     */
    static <E, L, R, A, U> Collector<E, ?, Result<L, U>> traversing(@NonNull Function<? super E, Result<L, R>> f,
                                                                    @NonNull Collector<? super R, A, U> downstream) {
        return Collectors.mapping(f, sequencing(downstream));
    }

    /**
     * Flatten nested results.
     *
     * @param result nested results
     * @param <E>    result error type
     * @param <R>    result success type
     * @return flattened result value
     */
    static <E, R> Result<E, R> flatten(@NonNull Result<? extends E, Result<? extends E, R>> result) {
        Result<E, Result<? extends E, R>> narrowed = cast(result);
        return narrowed.flatMap(Function.identity());
    }

    /**
     * Combine two results into single one.
     *
     * @param result1 first result
     * @param result2 second result
     */
    static <E, R1, R2> ResultApply2<E, R1, R2> ap(@NonNull Result<? extends E, R1> result1, @NonNull Result<? extends E, R2> result2) {
        // This cast is correct because <E> is at a covariant position.
        // Because it is at a covariant position, the <E> type is a supertype for
        // result1 and result2 error types, it's correct to cast it to <E>.
        return new ResultApply2<>(cast(result1), cast(result2));
    }

    /**
     * Intermediate class to combine two result values.
     *
     * @param <E>  error type
     * @param <R1> first success type
     * @param <R2> second success type
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class ResultApply2<E, R1, R2> {

        private final Result<E, R1> result1;
        private final Result<E, R2> result2;

        /**
         * Apply combine function.
         *
         * @param f   function
         * @param <R> new result value type
         * @return combined result
         */
        public <R> Result<E, R> apply(@NonNull BiFunction<? super R1, ? super R2, R> f) {
            return result1.flatMap(r1 -> result2.map(r2 -> f.apply(r1, r2)));
        }
    }
}

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
final class Error<E, R> implements Result<E, R> {

    private final E error;

    @Override
    public boolean isError() {
        return true;
    }

    @Override
    public R get() {
        throw new IllegalStateException("Result is error");
    }

    @Override
    public R getOrElse(@NonNull R other) {
        return other;
    }

    @Override
    public R getOrElse(@NonNull Function<? super E, ? extends R> other) {
        return other.apply(error);
    }

    @Override
    public <T extends Throwable> R getOrThrow(@NonNull Function<? super E, T> errorF) throws T {
        throw errorF.apply(error);
    }

    @Override
    public E getError() {
        return error;
    }

    @Override
    public <V> Result<E, V> map(@NonNull Function<? super R, V> f) {
        return cast(this);
    }

    @Override
    public <V> Result<E, V> flatMap(@NonNull Function<? super R, Result<? extends E, V>> f) {
        return cast(this);
    }

    @Override
    public <O> Result<O, R> mapError(@NonNull Function<? super E, O> f) {
        return Result.error(f.apply(error));
    }

    @Override
    public <O, V> Result<O, V> bimap(@NonNull Function<? super E, O> errorF, @NonNull Function<? super R, V> successF) {
        return Result.error(errorF.apply(error));
    }

    @Override
    public Result<E, R> filter(@NonNull Predicate<? super R> predicate, @NonNull Supplier<? extends E> errorF) {
        return this;
    }

    @Override
    public Result<R, E> swap() {
        return Result.success(error);
    }

    @Override
    public Result<E, R> peek(Consumer<? super R> f) {
        return this;
    }

    @Override
    public Result<E, R> peekError(Consumer<? super E> f) {
        f.accept(error);
        return this;
    }

    @Override
    public Result<E, R> orElse(@NonNull Function<? super E, Result<? extends E, ? extends R>> otherF) {
        return cast(otherF.apply(error));
    }
}

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
final class Ok<E, R> implements Result<E, R> {

    private final R value;

    @Override
    public boolean isError() {
        return false;
    }

    @Override
    public R get() {
        return value;
    }

    @Override
    public R getOrElse(@NonNull R other) {
        return value;
    }

    @Override
    public R getOrElse(@NonNull Function<? super E, ? extends R> other) {
        return value;
    }

    @Override
    public <T extends Throwable> R getOrThrow(@NonNull Function<? super E, T> errorF) throws T {
        return value;
    }

    @Override
    public E getError() {
        throw new IllegalStateException("Result is success");
    }

    @Override
    public <V> Result<E, V> map(@NonNull Function<? super R, V> f) {
        return Result.success(f.apply(value));
    }

    @Override
    public <V> Result<E, V> flatMap(@NonNull Function<? super R, Result<? extends E, V>> f) {
        return cast(f.apply(value));
    }

    @Override
    public <O> Result<O, R> mapError(@NonNull Function<? super E, O> f) {
        return cast(this);
    }

    @Override
    public <O, V> Result<O, V> bimap(@NonNull Function<? super E, O> errorF, @NonNull Function<? super R, V> successF) {
        return Result.success(successF.apply(value));
    }

    @Override
    public Result<E, R> filter(@NonNull Predicate<? super R> predicate, @NonNull Supplier<? extends E> errorF) {
        if (predicate.test(value)) {
            return this;
        }
        return Result.error(errorF.get());
    }

    @Override
    public Result<R, E> swap() {
        return Result.error(value);
    }

    @Override
    public Result<E, R> peek(Consumer<? super R> f) {
        f.accept(value);
        return this;
    }

    @Override
    public Result<E, R> peekError(Consumer<? super E> f) {
        return this;
    }

    @Override
    public Result<E, R> orElse(@NonNull Function<? super E, Result<? extends E, ? extends R>> otherF) {
        return this;
    }
}