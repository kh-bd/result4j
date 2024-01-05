package dev.khbd.result4j.core;

import static dev.khbd.result4j.core.Utils.cast;

import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Either data type.
 *
 * @param <L> left value type
 * @param <R> right value type
 * @author Sergei_Khadanovich
 */
public interface Either<L, R> {

    /**
     * Is either left.
     *
     * @return {@literal true} if either is left and {@literal false} otherwise
     */
    boolean isLeft();

    /**
     * Is either right.
     *
     * @return {@literal true} if either is right and {@literal false} otherwise
     */
    boolean isRight();

    /**
     * Convert current value to stream (right based).
     */
    default Stream<R> toStream() {
        return rightToStream();
    }

    /**
     * Convert current value to stream (right based).
     */
    Stream<R> rightToStream();

    /**
     * Convert current value to stream (left based).
     */
    Stream<L> leftToStream();

    /**
     * Convert to optional based on right value.
     */
    default Optional<R> toOptional() {
        return rightToOptional();
    }

    /**
     * Convert to optional based on right value.
     */
    Optional<R> rightToOptional();

    /**
     * Convert to optional based on left value.
     */
    Optional<L> leftToOptional();

    /**
     * Get left value.
     *
     * @return left value if either is left and throw error otherwise
     */
    L getLeft();

    /**
     * Get left value or compute result from supplied function.
     *
     * @param f fallback function
     * @return left value
     */
    L getLeftOrElse(Function<? super R, ? extends L> f);

    /**
     * Get left value or default value.
     *
     * @param defaultValue default value
     */
    L getLeftOrElse(L defaultValue);

    /**
     * Get left value or throw error.
     *
     * @param errorF error function
     * @param <E>    exception type
     * @return left value
     */
    <E extends Throwable> L getLeftOrThrow(Function<? super R, E> errorF) throws E;

    /**
     * Get right value.
     *
     * @return right value if either is right and throw error otherwise
     */
    R getRight();

    /**
     * Get right value or compute result from supplied function.
     *
     * @param f fallback function
     * @return right value
     */
    R getRightOrElse(Function<? super L, ? extends R> f);

    /**
     * Get right or default value.
     *
     * @param defaultValue default value
     */
    R getRightOrElse(R defaultValue);

    /**
     * Get right value or throw error.
     *
     * @param errorF error function
     * @param <E>    exception type
     * @return right value
     */
    <E extends Throwable> R getRightOrThrow(Function<? super L, E> errorF) throws E;

    /**
     * Transform right value.
     *
     * @param f    transformation function
     * @param <R1> new right type
     * @return transformed either
     */
    default <R1> Either<L, R1> map(Function<? super R, ? extends R1> f) {
        return mapRight(f);
    }

    /**
     * Transform right value.
     *
     * @param f    transformation function
     * @param <R1> new right type
     * @return transformed either
     */
    <R1> Either<L, R1> mapRight(Function<? super R, ? extends R1> f);

    /**
     * Transform left value.
     *
     * @param f    transformation function
     * @param <L1> new left type
     * @return transformed either
     */
    <L1> Either<L1, R> mapLeft(Function<? super L, ? extends L1> f);

    /**
     * Transform right value.
     *
     * @param f    transformation function
     * @param <R1> new right type
     * @return transformed either
     */
    default <R1> Either<L, R1> flatMap(Function<? super R, Either<? extends L, ? extends R1>> f) {
        return flatMapRight(f);
    }

    /**
     * Transform right value.
     *
     * @param f    transformation function
     * @param <R1> new right type
     * @return transformed either
     */
    <R1> Either<L, R1> flatMapRight(Function<? super R, Either<? extends L, ? extends R1>> f);

    /**
     * Transform left value.
     *
     * @param f    transformation function
     * @param <L1> new left type
     * @return transformed either
     */
    <L1> Either<L1, R> flatMapLeft(Function<? super L, Either<? extends L1, ? extends R>> f);

    /**
     * Transformed left and right values together.
     *
     * @param leftF  left transformation function
     * @param rightF right transformation function
     * @param <L1>   new left type
     * @param <R1>   new right type
     * @return transformed either
     */
    default <L1, R1> Either<L1, R1> biMap(Function<? super L, ? extends L1> leftF,
                                          Function<? super R, ? extends R1> rightF) {
        return biFlatMap(l -> Either.left(leftF.apply(l)), r -> Either.right(rightF.apply(r)));
    }

    /**
     * Transform left and right values together.
     *
     * @param leftF  left transformation function
     * @param rightF right transformation function
     * @param <L1>   new left type
     * @param <R1>   new right type
     * @return transformed either
     */
    <L1, R1> Either<L1, R1> biFlatMap(Function<? super L, Either<? extends L1, ? extends R1>> leftF,
                                      Function<? super R, Either<? extends L1, ? extends R1>> rightF);

    /**
     * Combine both either by function.
     *
     * @param other other either instance
     * @param func  combinator function
     * @param <R1>  other either right type
     * @param <R2>  combined either right type
     * @return combined either
     */
    default <R1, R2> Either<L, R2> zip(Either<? extends L, ? extends R1> other,
                                       BiFunction<? super R, ? super R1, ? extends R2> func) {
        return zipRight(other, func);
    }

    /**
     * Combine both either by function.
     *
     * @param other other either instance
     * @param func  combinator function
     * @param <R1>  other either right type
     * @param <R2>  combined either right type
     * @return combined either
     */
    default <R1, R2> Either<L, R2> zipRight(Either<? extends L, ? extends R1> other,
                                            BiFunction<? super R, ? super R1, ? extends R2> func) {
        return zipRightF(other, (v1, v2) -> Either.right(func.apply(v1, v2)));
    }

    /**
     * Combine both either by function.
     *
     * @param other other either instance
     * @param func  combinator function
     * @param <R1>  other either right type
     * @param <R2>  combined either right type
     * @return combined either
     */
    default <R1, R2> Either<L, R2> zipF(Either<? extends L, ? extends R1> other,
                                        BiFunction<? super R, ? super R1, Either<? extends L, ? extends R2>> func) {
        return zipRightF(other, func);
    }

    /**
     * Combine both either by function.
     *
     * @param other other either instance
     * @param func  combinator function
     * @param <R1>  other either right type
     * @param <R2>  combined either right type
     * @return combined either
     */
    default <R1, R2> Either<L, R2> zipRightF(Either<? extends L, ? extends R1> other,
                                             BiFunction<? super R, ? super R1, Either<? extends L, ? extends R2>> func) {
        Either<L, R1> narrowed = cast(other);
        return flatMapRight(v1 -> narrowed.flatMapRight(v2 -> func.apply(v1, v2)));
    }

    /**
     * Combine both either by function.
     *
     * @param other other either instance
     * @param func  combinator function
     * @param <L1>  other left type
     * @param <L2>  combined left type
     * @return combined either
     */
    default <L1, L2> Either<L2, R> zipLeft(Either<? extends L1, ? extends R> other,
                                           BiFunction<? super L, ? super L1, ? extends L2> func) {
        return zipLeftF(other, (v1, v2) -> Either.left(func.apply(v1, v2)));
    }

    /**
     * Combine both either by function.
     *
     * @param other other either instance
     * @param func  combinator function
     * @param <L1>  other either left type
     * @param <L2>  combined either left type
     * @return combined either
     */
    default <L1, L2> Either<L2, R> zipLeftF(Either<? extends L1, ? extends R> other,
                                            BiFunction<? super L, ? super L1, Either<? extends L2, ? extends R>> func) {
        Either<L1, R> narrowed = cast(other);
        return flatMapLeft(v1 -> narrowed.flatMapLeft(v2 -> func.apply(v1, v2)));
    }

    /**
     * Swap left and right sides.
     *
     * @return swapped either value.
     */
    Either<R, L> swap();

    /**
     * Invoke side effect function if either is right.
     *
     * @param f function
     */
    default void ifRight(Consumer<? super R> f) {
        ifBoth(l -> {
        }, f);
    }

    /**
     * Invoke side effect function if either is left.
     *
     * @param f function
     */
    default void ifLeft(Consumer<? super L> f) {
        ifBoth(f, r -> {
        });
    }

    /**
     * Invoke side effect function.
     *
     * @param leftF  left function
     * @param rightF right function
     */
    void ifBoth(Consumer<? super L> leftF, Consumer<? super R> rightF);

    /**
     * Peek right value and call function.
     *
     * @param f function to execute
     */
    default Either<L, R> peekRight(Consumer<? super R> f) {
        ifRight(f);
        return this;
    }

    /**
     * Peek left value and call function.
     *
     * @param f function to execute
     */
    default Either<L, R> peekLeft(Consumer<? super L> f) {
        ifLeft(f);
        return this;
    }

    /**
     * Peek right or left value and call appropriate function.
     *
     * @param leftF  left function to execute
     * @param rightF right function to execute
     */
    default Either<L, R> peekBoth(Consumer<? super L> leftF, Consumer<? super R> rightF) {
        ifBoth(leftF, rightF);
        return this;
    }

    /**
     * Recover left either instance.
     *
     * @param other other instance
     * @return current instance if it is right or other instance
     */
    default Either<L, R> orElse(Either<? extends L, ? extends R> other) {
        return rightOrElse(other);
    }

    /**
     * Recover left either instance.
     *
     * @param otherF other instance function
     * @return current instance if it is right or other instance
     */
    default Either<L, R> orElse(Function<? super L, Either<? extends L, ? extends R>> otherF) {
        return rightOrElse(otherF);
    }

    /**
     * Recover left either instance.
     *
     * @param other other instance
     * @return current instance if it is right or other instance
     */
    default Either<L, R> rightOrElse(Either<? extends L, ? extends R> other) {
        return rightOrElse(left -> other);
    }

    /**
     * Recover left either instance.
     *
     * @param otherF other instance function
     * @return current instance if it is right or other instance
     */
    Either<L, R> rightOrElse(Function<? super L, Either<? extends L, ? extends R>> otherF);

    /**
     * Recover right either instance.
     *
     * @param other other instance
     * @return current instance if it is left or other instance
     */
    default Either<L, R> leftOrElse(Either<? extends L, ? extends R> other) {
        return leftOrElse(right -> other);
    }

    /**
     * Recover right either instance.
     *
     * @param otherF other instance function
     * @return current instance if it is left or other instance
     */
    Either<L, R> leftOrElse(Function<? super R, Either<? extends L, ? extends R>> otherF);

    /**
     * Filter either right value if either is right.
     *
     * @param predicate filter predicate
     * @param leftF     default error supplier
     * @return filtered either
     */
    default Either<L, R> filter(Predicate<? super R> predicate, Supplier<? extends L> leftF) {
        return rightFilter(predicate, leftF);
    }

    /**
     * Filter either right value if either is right.
     *
     * @param predicate filter predicate
     * @param leftF     default left value supplier
     * @return filtered either
     */
    Either<L, R> rightFilter(Predicate<? super R> predicate, Supplier<? extends L> leftF);

    /**
     * Filter either left value if either is left.
     *
     * @param predicate filter predicate
     * @param rightF    default right value supplier
     * @return filtered either
     */
    Either<L, R> leftFilter(Predicate<? super L> predicate, Supplier<? extends R> rightF);

    /**
     * Create left instance.
     *
     * @param left left value
     * @param <L>  left value type
     * @param <R>  right value type
     * @return left instance
     */
    static <L, R> Either<L, R> left(L left) {
        return new Left<>(left);
    }

    /**
     * Create right instance.
     *
     * @param right right value
     * @param <L>   left value type
     * @param <R>   right value type
     * @return right instance
     */
    static <L, R> Either<L, R> right(R right) {
        return new Right<>(right);
    }

    /**
     * Sequence by right values.
     *
     * @param list list of either
     * @param <L>  left type
     * @param <R>  right type
     * @return traversed either
     */
    static <L, R> Either<L, List<R>> sequence(List<Either<L, R>> list) {
        return traverse(list, Function.identity());
    }

    /**
     * Sequence by right values.
     *
     * @param list list of either
     * @param <L>  left type
     * @param <R>  right type
     * @return traversed either
     */
    static <L, R> Either<L, List<R>> rightSequence(List<Either<L, R>> list) {
        return rightTraverse(list, Function.identity());
    }

    /**
     * Traverse by left values.
     *
     * @param list list of either
     * @param <L>  left type
     * @param <R>  right type
     * @return traversed either
     */
    static <L, R> Either<List<L>, R> leftSequence(List<Either<L, R>> list) {
        return leftTraverse(list, Function.identity());
    }

    /**
     * Traverse by right values.
     *
     * @param list list of either
     * @param f    transformation function
     * @param <L>  left type
     * @param <R>  right type
     * @param <U>  new right type
     * @return traversed either
     */
    static <L, R, U> Either<L, List<U>> traverse(List<R> list,
                                                 Function<? super R, Either<? extends L, ? extends U>> f) {
        return rightTraverse(list, f);
    }

    /**
     * Traverse by right values.
     *
     * @param list list of either
     * @param f    transformation function
     * @param <L>  left type
     * @param <R>  right type
     * @param <U>  new right type
     * @return right if all either were right and left if any either was left
     */
    static <L, R, U> Either<L, List<U>> rightTraverse(List<R> list,
                                                      Function<? super R, Either<? extends L, ? extends U>> f) {
        List<U> result = new ArrayList<>();
        for (R r : list) {
            Either<? extends L, ? extends U> either = f.apply(r);
            if (either.isLeft()) {
                return Either.left(either.getLeft());
            }
            result.add(either.getRight());
        }
        return Either.right(result);
    }

    /**
     * Traverse by left values.
     *
     * @param list list of either
     * @param f    transformation function
     * @param <L>  left type
     * @param <R>  right type
     * @param <U>  new left type
     * @return traversed either
     */
    static <L, R, U> Either<List<U>, R> leftTraverse(List<L> list,
                                                     Function<? super L, Either<? extends U, ? extends R>> f) {
        List<U> result = new ArrayList<>();
        for (L l : list) {
            Either<? extends U, ? extends R> either = f.apply(l);
            if (either.isRight()) {
                return Either.right(either.getRight());
            }
            result.add(either.getLeft());
        }
        return Either.left(result);
    }

    /**
     * Flatten right-based either.
     *
     * @param either either instance
     * @param <L>    result left type
     * @param <R>    right type
     * @param <L1>   outer left type
     * @param <L2>   inner left type
     * @return flattened either instance
     * @see #rightFlatten(Either)
     */
    static <L, L1 extends L, L2 extends L, R> Either<L, R> flatten(Either<L1, Either<L2, R>> either) {
        return rightFlatten(either);
    }

    /**
     * Flatten right-based either.
     *
     * @param either either instance
     * @param <L>    result left type
     * @param <R>    right type
     * @param <L1>   outer left type
     * @param <L2>   inner left type
     * @return flattened either instance
     */
    static <L, L1 extends L, L2 extends L, R> Either<L, R> rightFlatten(Either<L1, Either<L2, R>> either) {
        Either<L, Either<? extends L, R>> narrowed = cast(either);
        return narrowed.flatMapRight(Function.identity());
    }

    /**
     * Flatten left-based either.
     *
     * @param either either instance
     * @param <L>    result left type
     * @param <R>    result right type
     * @param <R1>   outer right type
     * @param <R2>   inner right type
     * @return flattened either instance
     */
    static <L, R, R1 extends R, R2 extends R> Either<L, R> leftFlatten(Either<Either<L, R1>, R2> either) {
        Either<Either<L, ? extends R>, R> narrowed = cast(either);
        return narrowed.flatMapLeft(Function.identity());
    }

    /**
     * Factory method to create either from element which may be null.
     *
     * @param right right value
     * @param <R>   right value type
     * @return evaluated either
     */
    static <R> Either<?, R> fromNullable(R right) {
        return rightFromNullable(right);
    }

    /**
     * Factory method to create either from element which may be null.
     *
     * @param right right value
     * @param <R>   right value type
     * @return evaluated either
     */
    static <R> Either<?, R> rightFromNullable(R right) {
        return Objects.nonNull(right) ? Either.right(right) : Either.left("Element is null");
    }

    /**
     * Factory method to create either from element which may be null.
     *
     * @param left left value
     * @param <L>  left value type
     * @return evaluated either
     */
    static <L> Either<L, ?> leftFromNullable(L left) {
        return Objects.nonNull(left) ? Either.left(left) : Either.right("Element is null");
    }

    /**
     * Factory method to create either from optional element.
     *
     * @param right right value
     * @param <R>   right value type
     * @return evaluated either
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <R> Either<?, R> fromOptional(Optional<R> right) {
        return rightFromOptional(right);
    }

    /**
     * Factory method to create either from optional element.
     *
     * @param right right value
     * @param <R>   right value type
     * @return evaluated either
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <R> Either<?, R> rightFromOptional(Optional<R> right) {
        return right
                .map(Either::right)
                .orElseGet(() -> Either.left("Element is null"));
    }

    /**
     * Factory method to create either from optional element.
     *
     * @param left left value
     * @param <L>  right value type
     * @return evaluated either
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    static <L> Either<L, ?> leftFromOptional(Optional<L> left) {
        return left
                .map(Either::left)
                .orElseGet(() -> Either.right("Element is null"));
    }

    /**
     * Create either instance from try.
     *
     * @param tr  try instance
     * @param <V> value type
     * @return right either instance if try is success and left either instance otherwise
     */
    static <V> Either<Throwable, V> fromTry(Try<V> tr) {
        return rightFromTry(tr);
    }

    /**
     * Create either instance from try.
     *
     * @param tr  try instance
     * @param <V> value type
     * @return right either instance if try is success and left either instance otherwise
     */
    static <V> Either<Throwable, V> rightFromTry(Try<V> tr) {
        return tr.map(Either::<Throwable, V>right)
                .getOrElse(Either::left);
    }

    /**
     * Create either instance from try.
     *
     * @param tr  try instance
     * @param <V> value type
     * @return right either instance if try is success and left either instance otherwise
     */
    static <V> Either<V, Throwable> leftFromTry(Try<V> tr) {
        return tr.map(Either::<V, Throwable>left)
                .getOrElse(Either::right);
    }
}

/**
 * Either left variant.
 *
 * @author Sergei Khadanovich
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class Left<L, R> implements Either<L, R> {

    private final L left;

    @Override
    public Stream<L> leftToStream() {
        return Stream.of(left);
    }

    @Override
    public Stream<R> rightToStream() {
        return Stream.empty();
    }

    @Override
    public Optional<L> leftToOptional() {
        return Optional.of(left);
    }

    @Override
    public Optional<R> rightToOptional() {
        return Optional.empty();
    }

    @Override
    public void ifBoth(Consumer<? super L> leftF, Consumer<? super R> rightF) {
        leftF.accept(left);
    }

    @Override
    public boolean isLeft() {
        return true;
    }

    @Override
    public boolean isRight() {
        return false;
    }

    @Override
    public L getLeft() {
        return left;
    }

    @Override
    public <E extends Throwable> L getLeftOrThrow(Function<? super R, E> errorF) {
        return left;
    }

    @Override
    public L getLeftOrElse(Function<? super R, ? extends L> f) {
        return left;
    }

    @Override
    public L getLeftOrElse(L defaultValue) {
        return left;
    }

    @Override
    public R getRight() {
        throw new RuntimeException("Either is left.");
    }

    @Override
    public <E extends Throwable> R getRightOrThrow(Function<? super L, E> errorF) throws E {
        throw errorF.apply(left);
    }

    @Override
    public R getRightOrElse(Function<? super L, ? extends R> f) {
        return f.apply(left);
    }

    @Override
    public R getRightOrElse(R defaultValue) {
        return defaultValue;
    }

    @Override
    public Either<L, R> rightFilter(Predicate<? super R> predicate, Supplier<? extends L> errorF) {
        return this;
    }

    @Override
    public Either<L, R> leftFilter(Predicate<? super L> predicate, Supplier<? extends R> errorF) {
        if (predicate.test(left)) {
            return this;
        }
        return Either.right(errorF.get());
    }

    @Override
    public <R1> Either<L, R1> mapRight(Function<? super R, ? extends R1> f) {
        return Either.left(left);
    }

    @Override
    public <L1> Either<L1, R> mapLeft(Function<? super L, ? extends L1> f) {
        return Either.left(f.apply(left));
    }

    @Override
    public <R1> Either<L, R1> flatMapRight(Function<? super R, Either<? extends L, ? extends R1>> f) {
        return Either.left(left);
    }

    @Override
    public <L1> Either<L1, R> flatMapLeft(Function<? super L, Either<? extends L1, ? extends R>> f) {
        return cast(f.apply(left));
    }

    @Override
    public <L1, R1> Either<L1, R1> biFlatMap(Function<? super L, Either<? extends L1, ? extends R1>> leftF,
                                             Function<? super R, Either<? extends L1, ? extends R1>> rightF) {
        return cast(leftF.apply(left));
    }

    @Override
    public Either<R, L> swap() {
        return Either.right(left);
    }

    @Override
    public Either<L, R> rightOrElse(Function<? super L, Either<? extends L, ? extends R>> otherF) {
        return cast(otherF.apply(left));
    }

    @Override
    public Either<L, R> leftOrElse(Function<? super R, Either<? extends L, ? extends R>> otherF) {
        return this;
    }
}

/**
 * Either right variant.
 *
 * @author Sergei Khadanovich
 */
@ToString
@EqualsAndHashCode
@RequiredArgsConstructor
class Right<L, R> implements Either<L, R> {

    private final R right;

    @Override
    public Stream<L> leftToStream() {
        return Stream.empty();
    }

    @Override
    public Stream<R> rightToStream() {
        return Stream.of(right);
    }

    @Override
    public Optional<L> leftToOptional() {
        return Optional.empty();
    }

    @Override
    public Optional<R> rightToOptional() {
        return Optional.of(right);
    }

    @Override
    public void ifBoth(Consumer<? super L> leftF, Consumer<? super R> rightF) {
        rightF.accept(right);
    }

    @Override
    public boolean isLeft() {
        return false;
    }

    @Override
    public boolean isRight() {
        return true;
    }

    @Override
    public L getLeft() {
        throw new RuntimeException("Either is right.");
    }

    @Override
    public <E extends Throwable> L getLeftOrThrow(Function<? super R, E> errorF) throws E {
        throw errorF.apply(right);
    }

    @Override
    public L getLeftOrElse(Function<? super R, ? extends L> f) {
        return f.apply(right);
    }

    @Override
    public L getLeftOrElse(L defaultValue) {
        return defaultValue;
    }

    @Override
    public R getRight() {
        return right;
    }

    @Override
    public <E extends Throwable> R getRightOrThrow(Function<? super L, E> errorF) {
        return right;
    }

    @Override
    public R getRightOrElse(Function<? super L, ? extends R> f) {
        return right;
    }

    @Override
    public R getRightOrElse(R defaultValue) {
        return right;
    }

    @Override
    public Either<L, R> rightFilter(Predicate<? super R> predicate, Supplier<? extends L> errorF) {
        if (predicate.test(right)) {
            return this;
        }
        return Either.left(errorF.get());
    }

    @Override
    public Either<L, R> leftFilter(Predicate<? super L> predicate, Supplier<? extends R> errorF) {
        return Either.right(right);
    }

    @Override
    public <R1> Either<L, R1> mapRight(Function<? super R, ? extends R1> f) {
        return Either.right(f.apply(right));
    }

    @Override
    public <L1> Either<L1, R> mapLeft(Function<? super L, ? extends L1> f) {
        return Either.right(right);
    }

    @Override
    public <R1> Either<L, R1> flatMapRight(Function<? super R, Either<? extends L, ? extends R1>> f) {
        return cast(f.apply(right));
    }

    @Override
    public <L1> Either<L1, R> flatMapLeft(Function<? super L, Either<? extends L1, ? extends R>> f) {
        return Either.right(right);
    }

    @Override
    public <L1, R1> Either<L1, R1> biFlatMap(Function<? super L, Either<? extends L1, ? extends R1>> leftF,
                                             Function<? super R, Either<? extends L1, ? extends R1>> rightF) {
        return cast(rightF.apply(right));
    }

    @Override
    public Either<R, L> swap() {
        return Either.left(right);
    }

    @Override
    public Either<L, R> rightOrElse(Function<? super L, Either<? extends L, ? extends R>> otherF) {
        return this;
    }

    @Override
    public Either<L, R> leftOrElse(Function<? super R, Either<? extends L, ? extends R>> otherF) {
        return cast(otherF.apply(right));
    }
}