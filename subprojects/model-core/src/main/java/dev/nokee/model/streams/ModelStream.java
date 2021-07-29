package dev.nokee.model.streams;

import org.gradle.api.provider.Provider;

import java.util.Comparator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collector;

/**
 * Represent a live stream of model elements, a.k.a. domain objects.
 * The stream can be manipulated via one or more processor steps.
 * The stream must end with a terminal operation such as {@link #forEach(Consumer)} or {@link #collect(Collector)}.
 * Stream instance can be reused.
 *
 * A {@code ModelStream} is created from a {@link Topic} which represent current and future elements of a specific base type.
 *
 * @param <T>  type of elements
 */
public interface ModelStream<T> {
	/**
	 * Split this stream.
	 * {@link BranchedModelStream} can be used for routing the elements to different branches depending on evaluation against supplied predicates.
	 * Stream branching is a stateless element-by-element operation.
	 *
	 * @return a {@link BranchedModelStream} that provides methods for routing the elements to different branches, never null
	 */
	BranchedModelStream<T> split();

	/**
	 * Creates a new {@code ModelStream} that consists of all elements of this stream which satisfy the given predicate.
	 * All elements that do not satisfy the predicate are dropped.
	 * Stream filtering is a stateless element-by-element operation.
	 *
	 * @param predicate  a filter predicate that is applied to each element, must not be null
	 * @return a {@code ModelStream} that contains only those elements that satisfy the given predicate, never null
	 */
	ModelStream<T> filter(Predicate<? super T> predicate);

	/**
	 * Transform each element of the incoming stream into zero or more elements in the outgoing stream.
	 * The provided function is applied to each incoming element and computes zero or more outgoing elements.
	 * Thus, an input element {@code <T>} can be transformed into output elements {@code <T'>}, {@code <T''>}, etc.
	 * Stream flat mapping is a stateless element-by-element operation.
	 *
	 * @param mapper  a function that computes the new outgoing elements, must not be null
	 * @param <R>  the type of elements in the result stream
	 * @return a {@code ModelStream} that contains more or less new elements (possibly of different type), never null
	 */
	<R> ModelStream<R> flatMap(Function<? super T, ? extends Iterable<? extends R>> mapper);

	/**
	 * Transform each element of the incoming stream into a new element in the outgoing stream.
	 * The provided function is applied to each incoming element and computes a new outgoing element.
	 * Thus, an input element {@code <T>} can be transformed into output element {@code <T'>}.
	 * Stream mapping is a stateless element-by-element operation.
	 *
	 * @param mapper  a function that computes the outgoing element, must not be null
	 * @param <R>  the type of elements in the result stream
	 * @return a {@code ModelStream} that contains new elements (possibly of different type), never null
	 */
	<R> ModelStream<R> map(Function<? super T, ? extends R> mapper);

	/**
	 * Returns a stream consisting of the elements of this stream, sorted according to the provided {@code Comparator}.
	 *
	 * Stream sorting is a stateful operation.
	 * The operation will batches available elements together.
	 *
	 * @param comparator a non-interfering, stateless {@code Comparator} to be used to compare stream elements, must not be null
	 * @return a {@code ModelStream} with sorted elements, never null
	 */
	ModelStream<T> sorted(Comparator<? super T> comparator);

	/**
	 * Perform an action on each element of {@code ModelStream}.
	 * Stream peeking is a stateless element-by-element operation.
	 *
	 * Peek is a non-terminal operation that triggers a side effect (such as logging or statistic collection) and returns an unchanged stream.
	 *
	 * @param action  an action to perform on each element, must not be null
	 * @return itself, never null
	 */
	ModelStream<T> peek(Consumer<? super T> action);

	/**
	 * Perform an action on each record of {@code ModelStream}.
	 * Stream iterating is a stateless element-by-element operation.
	 * Note that this is a terminal operation that returns void.
	 *
	 * @param action  an action to perform on each element, must not be null
	 */
	void forEach(Consumer<? super T> action);


	<R, A> Provider<R> collect(Collector<? super T, A, R> collector);

	/**
	 * Create a {@code ModelStream} for the specified topic.
	 *
	 * @param topic  a topic to create a stream from, must not be null
	 * @param <T>  type of elements
	 * @return a {@code ModelStream} processing elements from the specified topic, never null
	 */
	static <T> ModelStream<T> of(Topic<? extends T> topic) {
		return new DefaultModelStream<>(topic);
	}

	/**
	 * Create an empty {@code ModelStream}.
	 * An empty stream will never have any elements to process.
	 *
	 * @param <T>  type of elements
	 * @return an empty {@code ModelStream}, never null
	 */
	static <T> ModelStream<T> empty() {
		return EmptyModelStream.INSTANCE.withNarrowedType();
	}
}
