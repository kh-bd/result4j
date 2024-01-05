package dev.khbd.result4j.core;

import static dev.khbd.result4j.core.Utils.cast;

import lombok.EqualsAndHashCode;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

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
        return value.map(Option::some)
                .orElseGet(Option::none);
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
    public <R> Option<R> map(Function<? super V, ? extends R> function) {
        return Option.fromNullable(function.apply(value));
    }
}