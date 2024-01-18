package dev.khbd.result4j.core;

import static dev.khbd.result4j.core.Utils.cast;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Value to represent erroneous computation.
 *
 * @param <V> value type
 * @author Sergei_Khadanovich
 */
public interface Try<V> {

    /**
     * Check is try erroneous or not.
     *
     * @return {@code true} if try contains error and {@code false} otherwise
     */
    boolean isFailure();

    /**
     * Check is try success or not.
     *
     * @return {@code true} if try is not contains error and {@code false} otherwise
     */
    boolean isSuccess();

    /**
     * Expose error object.
     *
     * @return error or throw {@code RuntimeException} if try contains value
     */
    Throwable getError();

    /**
     * Unwrap erroneous value.
     *
     * @return {@code value} if value was evaluated without any errors or rethrow runtime wrapper for caught error
     */
    V get();

    /**
     * Unwrap value or get value from fallback function.
     *
     * @param func fallback function
     * @return {@code value} if try is success or fallback value from function
     */
    V getOrElse(Function<Throwable, ? extends V> func);

    /**
     * Unwrap value or value from fallback function.
     *
     * @param func fallback function
     * @return {@code value} if try is success or fallback value from function
     */
    default V getOrElse(Supplier<? extends V> func) {
        return getOrElse(ex -> func.get());
    }

    /**
     * Unwrap value or throw specified exception.
     *
     * @param f function to change error which will be thrown
     * @return {@code value} if value was evaluated without any errors or throw specified exception
     */
    <E extends Throwable> V getOrElseThrow(Function<Throwable, ? extends E> f) throws E;

    /**
     * Transform container value if it exists.
     *
     * @param mapper mapping function
     * @param <U>    transformed value type
     * @return transformed try instance
     */
    <U> Try<U> map(Function<? super V, ? extends U> mapper);

    /**
     * Transform container value if it exists.
     *
     * @param flatMapper flat mapper
     * @param <U>        transformed value type
     * @return if try is erroneous, return new try with the same error and return transformed value otherwise
     */
    <U> Try<U> flatMap(Function<? super V, Try<U>> flatMapper);

    /**
     * Filter container value by predicate.
     *
     * @param predicate predicate
     * @return If try is erroneous, return new try with the same error. If try is not erroneous but predicate is not satisfied,
     * return new erroneous try. Return the same try instance, otherwise.
     */
    Try<V> filter(Predicate<? super V> predicate);

    /**
     * Filter container value by predicate.
     *
     * @param predicate predicate
     * @param errorF    error supplier
     * @return If try is erroneous, return new try with the same error. If try is not erroneous but predicate is not satisfied,
     * return new erroneous try. Return the same try instance, otherwise.
     */
    Try<V> filter(Predicate<? super V> predicate, Supplier<Throwable> errorF);

    /**
     * Zip with other value and apply function to them.
     *
     * @param other other try
     * @param func  combinator function
     * @param <U>   other try value type
     * @param <R>   result type
     * @return zipped and combined try value
     */
    default <U, R> Try<R> zip(Try<U> other, BiFunction<? super V, ? super U, ? extends R> func) {
        return zipF(other, (v1, v2) -> Try.success(func.apply(v1, v2)));
    }

    /**
     * Zip with other value and return function result.
     *
     * @param other other try
     * @param func  combinator function
     * @param <U>   other try value type
     * @param <R>   result type
     * @return combined try value
     */
    default <U, R> Try<R> zipF(Try<U> other, BiFunction<? super V, ? super U, Try<R>> func) {
        return flatMap(value -> other.flatMap(otherValue -> func.apply(value, otherValue)));
    }

    /**
     * Recover erroneous try into new try.
     *
     * @param recover recover function
     * @return recovered either
     */
    Try<V> orElse(Function<Throwable, ? extends V> recover);

    /**
     * Recover erroneous try into new try.
     *
     * @param recoverF recover function
     * @return recovered either
     */
    Try<V> orElseF(Function<Throwable, Try<? extends V>> recoverF);

    /**
     * Convert try to either.
     *
     * @return either instance
     * @see Either#fromTry(Try)
     */
    default Either<Throwable, V> toEither() {
        return Either.fromTry(this);
    }

    /**
     * Convert value to option.
     */
    Option<V> toOption();

    /**
     * Convert value to stream.
     */
    default Stream<V> toStream() {
        return map(Stream::of).getOrElse(e -> Stream.empty());
    }

    /**
     * Drop value.
     */
    default Try<NoData> drop() {
        return map(__ -> NoData.INSTANCE);
    }

    /**
     * If a value is present, invoke the specified consumer with the value,
     * otherwise do nothing.
     *
     * @param successF block to be executed if a value is present
     */
    void ifSuccess(Consumer<? super V> successF);

    /**
     * If an error is present, invoke the specified consumer with the error,
     * otherwise do nothing.
     *
     * @param failureF block to be executed if an error is present
     */
    void ifFailure(Consumer<? super Throwable> failureF);

    /**
     * Invoke callback depending on value.
     *
     * @param successF success callback
     * @param failureF failure callback
     */
    void ifBoth(Consumer<? super V> successF, Consumer<? super Throwable> failureF);

    /**
     * Peek success value.
     *
     * @param successF block to be executed if value is success
     */
    default Try<V> peekSuccess(Consumer<? super V> successF) {
        ifSuccess(successF);
        return this;
    }

    /**
     * Peek failure value.
     *
     * @param failureF block to be executed if value is failure
     */
    default Try<V> peekFailure(Consumer<? super Throwable> failureF) {
        ifFailure(failureF);
        return this;
    }

    /**
     * Invoke callback depending on value.
     *
     * @param successF success callback
     * @param failureF failure callback
     */
    default Try<V> peekBoth(Consumer<? super V> successF, Consumer<? super Throwable> failureF) {
        ifBoth(successF, failureF);
        return this;
    }

    /**
     * Unwrap call.
     *
     * <p>This is method with special support through compiler plugin.
     * <p>Invocation of this method is replaced with special statements at compile time.
     * <p>For example, we have a function which is able to divide integers
     * <pre>{@code
     *     Try<Integer> divide(Integer num, Integer den) {
     *         if (den == 0) {
     *              return Try.failure(new IllegalArgumentException("Division by zero));
     *         }
     *         return Try.success(num / den);
     *     }
     * }</pre>
     * and we need to write a function which sums results of two divisions.
     * Such function can be written with zip combinator.
     * <pre>{@code
     *     Try<Integer> sumDivide(Integer num1, Integer den1, Integer num2, Integer den2) {
     *          Try<Integer> r1 = divide(num1, den1);
     *          Try<Integer> r2 = divide(num2, den2);
     *          return Try.zip(r1, r2, (v1, v2) -> v1 + v2);
     *     }
     * }</pre>
     * This example is very simple but a bit complicated to read.
     * The same code be written like this
     * <pre>{@code
     *     Try<Integer> sumDivide(Integer num1, Integer den1, Integer num2, Integer den2) {
     *         Integer r1 = divide(num1, den1).unwrap();
     *         Integer r2 = divide(num2, den2).unwrap();
     *         return Try.success(r1 + r2);
     *     }
     * }</pre>
     *
     * <p>Unwrap call is transformed at compile time. Simplified code looks like this.
     * <pre>{@code
     *      // invocation like this
     *      Integer result = divide(num, den).unwrap();
     *
     *      // is going to be transformed into several statements
     *      Try<Integer> $$rev = divide(num, den);
     *      if ($$rev.isFailure()) {
     *          return Try.failure($$rev.getError());
     *      }
     *      Integer result = $$rev.get();
     * }</pre>
     */
    default V unwrap() {
        throw new UnsupportedOperationException("This is a method with special support at compile time");
    }

    /**
     * Wrap erroneous computation into {@code Try}.
     *
     * <p>Note: {@code code} will be evaluated eager.
     *
     * @param code erroneous computation
     * @param <T>  value type
     * @return try wrapper for erroneous computation
     */
    static <T> Try<T> ofNullable(@NonNull Supplier<? extends T> code) {
        try {
            return Try.successNullable(code.get());
        } catch (Throwable e) {
            if (Failure.FatalErrorChecker.isFatal(e)) {
                throw e;
            }
            return new Failure<>(e);
        }
    }

    /**
     * Wrap erroneous computation into {@code Try}.
     *
     * <p>Note: {@code code} will be evaluated eager.
     *
     * @param code erroneous computation
     * @param <T>  value type
     * @return try wrapper for erroneous computation
     */
    static <T> Try<T> of(@NonNull Supplier<? extends T> code) {
        try {
            return Try.success(code.get());
        } catch (Throwable e) {
            if (Failure.FatalErrorChecker.isFatal(e)) {
                throw e;
            }
            return new Failure<>(e);
        }
    }

    /**
     * Wrap erroneous computation into {@code Try}.
     *
     * <p>Note: {@code code} will be evaluated eager.
     *
     * @param code code to run
     */
    static Try<?> of(@NonNull Runnable code) {
        return of(() -> {
            code.run();
            return NoData.INSTANCE;
        });
    }

    /**
     * Create fake try wrapper from computed value.
     *
     * <p>Value can be null.
     *
     * @param value value
     * @param <T>   value type
     * @return try wrapper
     */
    static <T> Try<T> success(T value) {
        if (Objects.nonNull(value)) {
            return new Success<>(value);
        }
        return new Failure<>(new NullPointerException("Value should not be null"));
    }

    /**
     * Create fake try wrapper from computed value.
     *
     * @param value value
     * @param <T>   value type
     * @return try wrapper
     */
    static <T> Try<T> successNullable(T value) {
        return new Success<>(value);
    }

    /**
     * Create fake try wrapper from error.
     *
     * @param e   error
     * @param <T> value type
     * @return try wrapper
     */
    static <T> Try<T> failure(@NonNull Throwable e) {
        return new Failure<>(e);
    }

    /**
     * Create try instance from either.
     *
     * @param either either instance
     * @param <E>    error type
     * @param <T>    value type
     * @return success try instance if either is right and failure instance otherwise
     */
    static <E extends Throwable, T> Try<T> fromEither(Either<E, T> either) {
        return either.map(Try::success)
                .getRightOrElse(Try::failure);
    }

    /**
     * Convert list of try to try of list and change all values.
     *
     * @param list   list of try
     * @param mapper transformation function
     * @param <U>    value type
     * @param <K>    new value type
     * @return try of list
     */
    static <U, K> Try<List<K>> traverse(@NonNull List<U> list, Function<? super U, Try<K>> mapper) {
        List<K> result = new ArrayList<>();
        for (U u : list) {
            Try<K> mappedU = Try.flatten(Try.of(() -> mapper.apply(u)));
            if (mappedU.isFailure()) {
                return Try.failure(mappedU.getError());
            }
            result.add(mappedU.get());
        }
        return Try.success(result);
    }

    /**
     * Convert list of try to try of list.
     *
     * @param list list of try
     * @param <U>  value type
     * @return try of list
     */
    static <U> Try<List<U>> sequence(@NonNull List<Try<U>> list) {
        return traverse(list, Function.identity());
    }

    /**
     * Flatten nested try.
     *
     * @param nestedTry nested try instance
     * @param <V>       value type
     * @return flattened try
     */
    static <V> Try<V> flatten(@NonNull Try<Try<V>> nestedTry) {
        return nestedTry.flatMap(Function.identity());
    }

    /**
     * Create try wrapper from option value.
     *
     * @param option        option value
     * @param errorSupplier supplier for throwable
     * @param <T>           value type
     * @return try with value if option contains value and try with error otherwise
     */
    static <T> Try<T> fromOption(@NonNull Option<T> option, Supplier<Throwable> errorSupplier) {
        return option.map(Try::success).getOrElse(() -> Try.failure(errorSupplier.get()));
    }

    /**
     * Create try wrapper from option value.
     *
     * @param option option value
     * @param <T>    value type
     * @return try with value if option contains value and try with error otherwise
     */
    static <T> Try<T> fromOption(@NonNull Option<T> option) {
        return Try.of(option::get);
    }

}

/**
 * Success variant of try.
 *
 * @author Sergei Khadanovich
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class Success<V> implements Try<V> {

    private final V value;

    @Override
    public boolean isFailure() {
        return false;
    }

    @Override
    public boolean isSuccess() {
        return true;
    }

    @Override
    public Throwable getError() {
        throw new RuntimeException("Try is not erroneous");
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public V getOrElse(Function<Throwable, ? extends V> func) {
        return value;
    }

    @Override
    public <E extends Throwable> V getOrElseThrow(Function<Throwable, ? extends E> f) {
        return value;
    }

    @Override
    public <U> Try<U> map(Function<? super V, ? extends U> mapper) {
        return Try.of(() -> mapper.apply(value));
    }

    @Override
    public <U> Try<U> flatMap(Function<? super V, Try<U>> flatMapper) {
        try {
            return flatMapper.apply(value);
        } catch (Throwable e) {
            if (Failure.FatalErrorChecker.isFatal(e)) {
                throw e;
            }
            return Try.failure(e);
        }
    }

    @Override
    public Try<V> filter(Predicate<? super V> predicate) {
        return filter(predicate, () -> new RuntimeException("Value is not correspond specified predicate"));
    }

    @Override
    public Try<V> filter(Predicate<? super V> predicate, Supplier<Throwable> errorF) {
        try {
            return predicate.test(value) ? this : Try.failure(errorF.get());
        } catch (Throwable e) {
            if (Failure.FatalErrorChecker.isFatal(e)) {
                throw e;
            }
            return Try.failure(e);
        }
    }

    @Override
    public Try<V> orElse(Function<Throwable, ? extends V> recover) {
        return this;
    }

    @Override
    public Try<V> orElseF(Function<Throwable, Try<? extends V>> recoverF) {
        return this;
    }

    @Override
    public Option<V> toOption() {
        return Option.some(value);
    }

    @Override
    public void ifSuccess(Consumer<? super V> successF) {
        successF.accept(value);
    }

    @Override
    public void ifFailure(Consumer<? super Throwable> failureF) {
    }

    @Override
    public void ifBoth(Consumer<? super V> successF, Consumer<? super Throwable> failureF) {
        successF.accept(value);
    }
}

/**
 * Failure variant of try.
 *
 * @author Sergei Khadanovich
 */
@RequiredArgsConstructor
class Failure<V> implements Try<V> {

    private final Throwable ex;

    @Override
    public boolean isFailure() {
        return true;
    }

    @Override
    public boolean isSuccess() {
        return false;
    }

    @Override
    public Throwable getError() {
        return ex;
    }

    @Override
    public V get() {
        throw new RuntimeException("Try is erroneous", ex);
    }

    @Override
    public V getOrElse(Function<Throwable, ? extends V> func) {
        return func.apply(ex);
    }

    @Override
    public <E extends Throwable> V getOrElseThrow(Function<Throwable, ? extends E> f) throws E {
        throw f.apply(ex);
    }

    @Override
    public <U> Try<U> map(Function<? super V, ? extends U> mapper) {
        return Try.failure(ex);
    }

    @Override
    public <U> Try<U> flatMap(Function<? super V, Try<U>> flatMapper) {
        return Try.failure(ex);
    }

    @Override
    public Try<V> filter(Predicate<? super V> predicate) {
        return this;
    }

    @Override
    public Try<V> filter(Predicate<? super V> predicate, Supplier<Throwable> errorF) {
        return this;
    }

    @Override
    public Try<V> orElse(Function<Throwable, ? extends V> recoverF) {
        return Try.of(() -> recoverF.apply(ex));
    }

    @Override
    public Try<V> orElseF(Function<Throwable, Try<? extends V>> recoverF) {
        return cast(Try.flatten(Try.of(() -> recoverF.apply(ex))));
    }

    @Override
    public Option<V> toOption() {
        return Option.none();
    }

    @Override
    public void ifSuccess(Consumer<? super V> successF) {
    }

    @Override
    public void ifFailure(Consumer<? super Throwable> failureF) {
        failureF.accept(ex);
    }

    @Override
    public void ifBoth(Consumer<? super V> successF, Consumer<? super Throwable> failureF) {
        failureF.accept(ex);
    }

    @UtilityClass
    static class FatalErrorChecker {

        /**
         * Check if exception fatal or not.
         *
         * @param th exception instance
         * @return {@code true} if exception is fatal and {@code false} otherwise
         */
        static boolean isFatal(Throwable th) {
            return th instanceof VirtualMachineError
                    || th instanceof ThreadDeath
                    || th instanceof InterruptedException;
        }
    }
}