package dev.nokee.model.streams;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Represent the optional parameters when building branches with {@link BranchedModelStream}.
 *
 * @param <T>  type of element
 */
public interface Branched<T> {
	/**
	 * Create instance of a {@code Branched} with provided name.
	 *
	 * @param name  the branch name to be used (see {@link BranchedModelStream} description for details)
	 * @param <T>  type of element
	 * @return a new instance of {@code Branched}, never null
	 */
	static <T> Branched<T> as(String name) {
		return new Branches.Named<>(name);
	}

	/**
	 * Create instance of a {@code Branched} with provided chain consumer.
	 * The branch name can be customized via {@link #withName(String)} (see {@link BranchedModelStream} description for details).
	 *
	 * NOTE: The respective branch will be added to the resulting map returned by {@link BranchedModelStream#defaultBranch(Branched)} or {@link BranchedModelStream#noDefaultBranch()} (see {@link BranchedModelStream} description for details).
	 *
	 * @param chain  a consumer to which the branch will be sent, must not be null
	 * @param <T>  type of element
	 * @return a new instance of {@code Branched}, never null
	 */
	static <T> Branched<T> withConsumer(Consumer<? super ModelStream<T>> chain) {
		return new Branches.Consumed<>(chain);
	}

	/**
	 * Create instance of a {@code Branched} with provided chain function.
	 * The branch name can be customized via {@link #withName(String)} (see {@link BranchedModelStream} description for details).
	 *
	 * NOTE: If the provided function return {@code null}, its result is ignored, otherwise it is added to the map returned by {@link BranchedModelStream#defaultBranch(Branched)} or {@link BranchedModelStream#noDefaultBranch()} (see {@link BranchedModelStream} description for details).
	 *
	 * @param chain  a function that will be applied to the branch, must not be null
	 * @param <T>  type of element
	 * @return a new instance of {@code Branched}, never null
	 */
	static <T> Branched<T> withFunction(Function<? super ModelStream<T>, ? extends ModelStream<T>> chain) {
		return new Branches.Transformed<>(chain);
	}

	/**
	 * Configure the instance of {@code Branched} with the specified branch name.
	 *
	 * @param name  the branch name to use, must not be null
	 * @return {@code this} to facilitate method chaining, never null
	 */
	Branched<T> withName(String name);
}
