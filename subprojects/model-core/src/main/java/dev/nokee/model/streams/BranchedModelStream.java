package dev.nokee.model.streams;

import java.util.Map;
import java.util.function.Predicate;

/**
 * Branches the elements in the original stream based on the predicates supplied for the branch definition.
 *
 * Branches are defined with {@link #branch(Predicate, Branched)} or {@link #defaultBranch(Branched)} methods.
 * Each elements is evaluated against the predicates supplied via {@link Branched} parameters, and is routed to the first branch for which its respective predicate evaluate to {@code true}.
 * If an element does not match any predicates, it will be routed to the default branch, or dropped if no default branch is created.
 *
 * Each branch (which is a {@link ModelStream} instance) then can be processed either by a {@link java.util.function.Function} or a {@link java.util.function.Consumer} provided via a {@link Branched} parameter.
 * If certain condition are met, it also can be accessed from the {@link Map} returned by an optional {@link #defaultBranch(Branched)} or {@link #noDefaultBranch()} method call (see <a href="#examples">usage examples</a>).
 *
 * The branching happens on a first-match basis: An element in the original stream is assigned to the corresponding result stream for the first predicate that evaluates to {@code true}, and is assigned to this stream only.
 * If you need to route an element to multiple streams, you can apply multiple {@link ModelStream#filter(Predicate)} operators to the same {@link ModelStream} instance, one for each predicate, instead of branching.
 *
 * The process of routing the elements to different branches is a stateless element-by-element operation.
 *
 * <h2><a name="maprules">Rules of forming the resulting map</a></h2>
 *
 * The keys of the {@code Map<String, ModelStream<T>>} entries returned by {@link #defaultBranch(Branched)} or {@link #noDefaultBranch()} are defined by the following rules:
 * <ul>
 *     <li>If a branch name is provided in {@link #branch(Predicate, Branched)} via the {@link Branched} parameter, its value is used as the {@code Map} key</li>
 *     <li>If a name is not provided for the branch, then the key defaults to {@code position} of the branch as a decimal number, starting from {@code "1"}</li>
 *     <li>If a name is not provided for the {@link #defaultBranch(Branched)} call, then the key defaults to {@code "0"}</li>
 * </ul>
 *
 * The values of the respective {@code Map<String, ModelStream<T>>} entries are formed as following:
 * <ul>
 *     <li>If no chain function or consumer is provided in {@link #branch(Predicate, Branched)} or {@link #defaultBranch(Branched)} via {@link Branched} parameter, then the branch itself is added to the map</li>
 *     <li>If chain function is provided and it returns a non-null value for a given branch, then the value is the result returned by this function</li>
 *     <li>If a chain function returns {@code null} for a given branch, then no entry is added to the map</li>
 *     <li>If a consumer is provided, then the branch itself is added to the map</li>
 * </ul>
 *
 * For example:
 * <code>
 * Map<String, ModelStream<...>> result =
 *   source.split()
 *     .branch(predicate1, Branched.as("bar"))							// "bar": a name is provided
 *     .branch(predicate2, Branched.withConsumer(s->s.forEach(...))) 	// "2": name defaults to branch position for the stream passed to the consumer
 *     .branch(predicate3, Branched.withFunction(s->null))           	// no entry: chain function returns null
 *     .branch(predicate4)											 	// "4": name defaults to branch position
 *     .defaultBranch()													// "0": "0" is the default name for the default branch
 * </code>
 *
 * <h2><a name="examples">Usage examples</a></h2>
 *
 * <h3>Direct branch consuming</h3>
 *
 * In many cases we do not need to have a single scope for all branches, each branch being processed completely independently from others.
 * Then we can use 'consuming' lambdas or method references in {@link Branched} parameter:
 *
 * <code>
 * stream.split()
 * 		.branch(predicate1, Branched.withConsumer(s -> s.forEach(...))
 * 		.branch(predicate2, Branched.withConsumer(s -> s.forEach(...))
 * 		.defaultBranch(Branched.withConsumer(s -> s.forEach(...))
 * </code>
 *
 * <h3>Collecting branches in a single scope</h3>
 *
 * In other cases we want to collect branches elements after splitting.
 * The map returned by {@link #defaultBranch(Branched)} or {@link #noDefaultBranch()} methods provides access to all the branches in the same scope:
 *
 * <code>
 * Map<String, ModelStream> branches = stream.split()
 * 		.branch(isDebugBinaries(), Branched.withFunction(s -> s.map(toLinkedFile()).withName("debug"))
 * 		.defaultBranch(Branched.as("release"))
 *
 * Provider<List> debugBinaries = branches.get("debug").collect(Collectors.toList())
 * Provider<List> releaseBinaries = branches.get("release").collect(Collectors.toList())
 * </code>
 *
 * <h3>Dynamic branching</h3>
 *
 * There is also a case when we might need to create branches dynamically, e.g. one per enum value:
 *
 * <code>
 *  BranchedModelStream branched = stream.split();
 *  for (BuildType buildType : BuildType.values())
 *      branched.branch(it -> it.getBuildType() == buildType,
 *          Branched.withConsumer(buildType::processBinaries));
 * </code>
 *
 * @param <T>  type of elements
 * @see ModelStream
 */
// TODO: Ensure consumer branch are not added in the model stream
// TODO: Ensure consumer branched can't be named
// TODO: Following predicate should not return true
public interface BranchedModelStream<T> {
	/**
	 * Define a branch for records that match the predicate.
	 * The branch name will default to its index within the branch definition, see <rules of forming branch>.
	 *
	 * @param predicate  A {@link Predicate} instance, against which each element will be evaluated.
	 *                   If this predicate returns {@code true} for a given element, the element will be routed to the current branch and will not be evaluated against the predicates for the remaining branches.
	 *                   Must not be null.
	 * @return {@code this} to facilitate method chaining, never null
	 */
	BranchedModelStream<T> branch(Predicate<? super T> predicate);

	/**
	 * Define a branch for records that match the predicate.
	 * The branch name will default to its index within the branch definition unless a custom name is provided via the {@link Branched} parameter, see <a href="#maprules">rules of forming the resulting map</a>.
	 *
	 * @param predicate  A {@link Predicate} instance, against which each element will be evaluated.
	 *                   If this predicate returns {@code true} for a given element, the element will be routed to the current branch and will not be evaluated against the predicates for the remaining branches.
	 *                   Must not be null.
	 * @param branched  A {@link Branched} instance, that allows to define a branch name, an in-place branch consumer or branch mapper (see <a href="#examples">code examples for BranchedModelStream</a>).
	 *                  Must not be null.
	 * @return {@code this} to facilitate method chaining, never null
	 */
	BranchedModelStream<T> branch(Predicate<? super T> predicate, Branched<T> branched);

	/**
	 * Finalize the construction of branches and defines the default branch for the elements not intercepted by other branches.
	 * Calling {@code defaultBranch()} or {@link #noDefaultBranch()} is optional.
	 *
	 * @return {@link Map} of named branches, never null. See <a href="#maprule">rules of forming the resulting map</a> for more information.
	 */
	Map<String, ModelStream<T>> defaultBranch();

	/**
	 * Finalize the construction of branches and defines the default branch for the elements not intercepted by other branches.
	 * Calling {@code defaultBranch(Branched)} or {@link #noDefaultBranch()} is optional.
	 *
	 * @param branched  a {@link Branched} instance, that allows to define a branch name, an in-place branch consumer or branch mapper (see <a href="#examples">code examples for BranchedModelStream</a>)
	 * @return {@link Map} of named branches, never null. See <a href="#maprule">rules of forming the resulting map</a> for more information.
	 */
	Map<String, ModelStream<T>> defaultBranch(Branched<T> branched);

	/**
	 * Finalize the construction of branches without forming a default branch for the elements not intercepted by other branches.
	 * Calling {@link #defaultBranch(Branched)} or {@code noDefaultBranch()} is optional.
	 *
	 * @return {@link Map} of named branches, never null. See <a href="#maprule">rules of forming the resulting map</a> for more information.
	 */
	Map<String, ModelStream<T>> noDefaultBranch();
}
