package dev.khbd.result4j.core;

import static dev.khbd.result4j.core.Utils.cast;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
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
 * Option data class.
 *
 * @param <V> value type
 * @author Sergei Khadanovich
 */
public interface Option<V> {

    /**
     * Check if current instance is empty or not.
     *
     * @return {@literal true} if option is empty and {@literal false} otherwise
     */
    boolean isEmpty();

    /**
     * Get internal value.
     *
     * @return internal value
     * @throws NoSuchElementException if option is empty
     */
    V get();

    /**
     * Get internal value or default value.
     *
     * @param defaultValue default value
     * @return internal value or default
     */
    V getOrElse(V defaultValue);

    /**
     * Get internal value or default value.
     *
     * @param defaultF default value provider
     * @return interval value or default
     */
    V getOrElse(Supplier<? extends V> defaultF);

    /**
     * Get internal value or throw error.
     *
     * @param errorF error provider
     * @param <E>    exception type
     * @return interval value
     * @throws E if option is empty
     */
    <E extends Throwable> V getOrElseThrow(Supplier<E> errorF) throws E;

    /**
     * Check internal by specified predicate.
     *
     * @param predicate predicate
     */
    Option<V> filter(Predicate<? super V> predicate);

    /**
     * Transform internal value by specified function.
     *
     * @param function transformer
     * @param <R>      new result type
     * @return transformed option
     */
    <R> Option<R> map(Function<? super V, ? extends R> function);

    /**
     * Transform internal value by specified function.
     *
     * @param function transformer
     * @param <R>      new result type
     * @return transformed option
     */
    <R> Option<R> flatMap(Function<? super V, Option<? extends R>> function);

    /**
     * Peek value if present.
     *
     * @param function value consumer
     * @return self option instance
     */
    Option<V> peek(Consumer<? super V> function);

    /**
     * Convert option instance to optional.
     */
    default Optional<V> toOptional() {
        return map(Optional::of).getOrElse(Optional::empty);
    }

    /**
     * Drop value.
     */
    default Option<NoData> drop() {
        return map(__ -> NoData.INSTANCE);
    }

    /**
     * Convert option instance to stream.
     */
    default Stream<V> toStream() {
        return map(Stream::of).getOrElse(Stream::empty);
    }

    /**
     * Recover option by another instance.
     *
     * @param other another instance
     */
    Option<V> orElse(Option<? extends V> other);

    /**
     * Recover option by another instance.
     *
     * @param otherF another instance factory
     */
    Option<V> orElse(Supplier<Option<? extends V>> otherF);

    /**
     * Unwrap call.
     *
     * <p>This is method with special support through compiler plugin.
     * <p>Invocation of this method is replaced with special statements at compile time.
     * <p>For example, we have a function which is able to divide integers
     * <pre>{@code
     *     Option<Integer> divide(Integer num, Integer den) {
     *         if (den == 0) {
     *              return Option.none();
     *         }
     *         return Option.some(num / den);
     *     }
     * }</pre>
     * and we need to write a function which sums results of two divisions.
     * Such function can be written with zip combinator.
     * <pre>{@code
     *     Option<Integer> sumDivide(Integer num1, Integer den1, Integer num2, Integer den2) {
     *          Option<Integer> r1 = divide(num1, den1);
     *          Option<Integer> r2 = divide(num2, den2);
     *          return Option.zip(r1, r2, (v1, v2) -> v1 + v2);
     *     }
     * }</pre>
     * This example is very simple but a bit complicated to read.
     * The same code be written like this
     * <pre>{@code
     *     Option<Integer> sumDivide(Integer num1, Integer den1, Integer num2, Integer den2) {
     *         Integer r1 = divide(num1, den1).unwrap();
     *         Integer r2 = divide(num2, den2).unwrap();
     *         return Option.some(r1 + r2);
     *     }
     * }</pre>
     *
     * <p>Unwrap call is transformed at compile time. Simplified code looks like this.
     * <pre>{@code
     *      // invocation like this
     *      Integer result = divide(num, den).unwrap();
     *
     *      // is going to be transformed into several statements
     *      Option<Integer> $$rev = divide(num, den);
     *      if ($$rev.isEmpty()) {
     *          return Option.none();
     *      }
     *      Integer result = $$rev.get();
     * }</pre>
     */
    default V unwrap() {
        throw new UnsupportedOperationException("This is a method with special support at compile time");
    }

    /**
     * Create empty option value.
     *
     * @param <V> value type
     * @return none option
     */
    static <V> Option<V> none() {
        return cast(None.INSTANCE);
    }

    /**
     * Create some option value.
     *
     * @param value value
     * @param <V>   value type
     * @return some option
     */
    static <V> Option<V> some(@NonNull V value) {
        return new Some<>(value);
    }

    /**
     * Create option value.
     *
     * @param value value
     * @param <V>   value type
     * @return some option if value is not null and none otherwise
     */
    static <V> Option<V> fromNullable(V value) {
        return Objects.isNull(value) ? none() : some(value);
    }

    /**
     * Create option value from {@link Optional}.
     *
     * @param value optional value
     * @param <V>   value type
     * @return some if supplied optional is not empty
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <V> Option<V> fromOptional(@NonNull Optional<V> value) {
        return value.map(Option::some).orElseGet(Option::none);
    }

    /**
     * Flatten nested option into flat one.
     *
     * @param value option value
     * @param <V>   value type
     */
    static <V> Option<V> flatten(@NonNull Option<Option<V>> value) {
        return value.flatMap(Function.identity());
    }

    /**
     * Create sequence collector.
     *
     * @param <V> value type
     * @param <A> downstream accumulator type
     * @param <U> downstream result type
     * @return sequence collector
     */
    static <V, A, U> Collector<Option<V>, ?, Option<U>> sequencing(@NonNull Collector<? super V, A, U> downstream) {
        Supplier<A> supplier = downstream.supplier();
        BiConsumer<A, ? super V> accumulator = downstream.accumulator();
        BinaryOperator<A> combiner = downstream.combiner();
        Function<A, U> finisher = downstream.finisher();

        return Collector.of(
                () -> new Ref<>(Option.some(supplier.get())),
                (ref, option) -> {
                    ref.ref = Option.ap(ref.ref, option).apply((acc, r) -> {
                        accumulator.accept(acc, r);
                        return acc;
                    });
                },
                (ref1, ref2) -> new Ref<>(Option.ap(ref1.ref, ref2.ref).apply(combiner)),
                ref -> ref.ref.map(finisher)
        );
    }

    /**
     * Create traverse collector.
     *
     * @param f          mapper function
     * @param downstream downstream collector
     * @param <V>        original value type
     * @param <R>        transformed value type
     * @param <A>        downstream accumulator type
     * @param <U>        downstream result type
     * @return traversing collector
     */
    static <V, R, A, U> Collector<V, ?, Option<U>> traversing(@NonNull Function<? super V, Option<R>> f,
                                                              @NonNull Collector<? super R, A, U> downstream) {
        return Collectors.mapping(f, sequencing(downstream));
    }

    /**
     * Combine two options into single one.
     *
     * @param option1 first option
     * @param option2 second option
     * @param <V1>    first type
     * @param <V2>    second type
     * @return combined option
     */
    static <V1, V2> OptionApply2<V1, V2> ap(@NonNull Option<V1> option1, @NonNull Option<V2> option2) {
        return new OptionApply2<>(option1, option2);
    }

    /**
     * Intermediate class to combine two option values.
     *
     * @param <V1> first option value type
     * @param <V2> second option value type
     */
    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    final class OptionApply2<V1, V2> {

        private final Option<V1> option1;
        private final Option<V2> option2;

        /**
         * Apply combine function.
         *
         * @param f   function
         * @param <V> new result type
         * @return combined option
         */
        public <V> Option<V> apply(@NonNull BiFunction<? super V1, ? super V2, V> f) {
            return option1.flatMap(v1 -> option2.map(v2 -> f.apply(v1, v2)));
        }
    }
}

@ToString
@EqualsAndHashCode
class None<V> implements Option<V> {

    static final None<Object> INSTANCE = new None<>();

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public V get() {
        throw new NoSuchElementException("Option is empty");
    }

    @Override
    public V getOrElse(V defaultValue) {
        return defaultValue;
    }

    @Override
    public V getOrElse(@NonNull Supplier<? extends V> defaultF) {
        return defaultF.get();
    }

    @Override
    public <E extends Throwable> V getOrElseThrow(@NonNull Supplier<E> errorF) throws E {
        throw errorF.get();
    }

    @Override
    public Option<V> filter(@NonNull Predicate<? super V> predicate) {
        return this;
    }

    @Override
    public <R> Option<R> map(@NonNull Function<? super V, ? extends R> function) {
        return Option.none();
    }

    @Override
    public <R> Option<R> flatMap(@NonNull Function<? super V, Option<? extends R>> function) {
        return Option.none();
    }

    @Override
    public Option<V> peek(@NonNull Consumer<? super V> function) {
        return this;
    }

    @Override
    public Option<V> orElse(@NonNull Option<? extends V> other) {
        return cast(other);
    }

    @Override
    public Option<V> orElse(@NonNull Supplier<Option<? extends V>> otherF) {
        return cast(otherF.get());
    }
}

@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class Some<V> implements Option<V> {

    private final V value;

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public V get() {
        return value;
    }

    @Override
    public V getOrElse(V defaultValue) {
        return value;
    }

    @Override
    public V getOrElse(@NonNull Supplier<? extends V> defaultF) {
        return value;
    }

    @Override
    public <E extends Throwable> V getOrElseThrow(@NonNull Supplier<E> errorF) {
        return value;
    }

    @Override
    public Option<V> filter(@NonNull Predicate<? super V> predicate) {
        if (predicate.test(value)) {
            return this;
        }
        return Option.none();
    }

    @Override
    public <R> Option<R> map(@NonNull Function<? super V, ? extends R> function) {
        return Option.fromNullable(function.apply(value));
    }

    @Override
    public <R> Option<R> flatMap(@NonNull Function<? super V, Option<? extends R>> function) {
        Option<? extends R> result = function.apply(value);
        Objects.requireNonNull(result, "Function result cannot be null.");
        return cast(result);
    }

    @Override
    public Option<V> peek(@NonNull Consumer<? super V> function) {
        function.accept(value);
        return this;
    }

    @Override
    public Option<V> orElse(@NonNull Option<? extends V> other) {
        return this;
    }

    @Override
    public Option<V> orElse(@NonNull Supplier<Option<? extends V>> otherF) {
        return this;
    }
}