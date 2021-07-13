package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import groovy.transform.stc.ClosureParams;
import groovy.transform.stc.FirstParam;
import groovy.transform.stc.FromString;
import groovy.transform.stc.SimpleType;
import org.gradle.api.Action;
import org.gradle.api.Named;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.util.ConfigureUtil;

import java.util.function.BiConsumer;

/**
 * Represent a node in the model.
 *
 * More lenient than core ModelNode, has conveniences for Groovy DSL.
 *
 * Creates projections based on bridged containers, see NokeeExtension#bridgeContainer.
 *
 * <pre>
 * plugins {
 *     id 'dev.nokee.model-base'
 * }
 *
 * nokee.model {
 *     def compileTask = test.compile(Exec) { // will register Exec task named 'compileTest'
 *         // executed when...
 *     }
 *     test {
 *         def runtimeOnly = runtimeOnly(Configuration) // will register Configuration named 'testRuntimeOnly'
 *         compileTask.configure {
 *             args '-cp', runtimeOnly.map { it.asPath }.get()
 *         }
 *     }
 * }
 * </pre>
 */
// TODO: We should not be talking in term of projection at this level but instead as domain objects
// TODO: Given BiAction is not public API, maybe we should consider using Java Consumer instead of Gradle Action
//   Gradle Action usually signal the decoration of Closure methods, but given that it gives a very bad experience in static/type-checked Groovy, we implement our own Closure method variants.
// TODO: The name of the class is "wrong" in the sense it should be different than core.ModelNode.
public interface ModelNode extends Named, NodePredicates {
	/**
	 * Returns child node matching the specified identity or create a new child node if absent.
	 *
	 * @param identity  a named or to-stringable object that represent the child node, must not be null
	 * @return the child node matching identity, never null
	 */
	ModelNode node(Object identity);

	/**
	 * Executes action on child node matching the specified identity; create a new child node if absent.
	 *
	 * @param identity  a named or to-stringable object that represent the child node, must not be null
	 * @param action  an action to execute on the child node matching identity, must not be null
	 * @return the child node matching identity, never null
	 */
	ModelNode node(Object identity, Action<? super ModelNode> action);

	/** @see #node(Object, Action) */
	default ModelNode node(Object identity, @ClosureParams(value = SimpleType.class, options = "dev.nokee.model.dsl.ModelNode") @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return node(identity, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns known object of the specified type from the child node matching the identity; creates a new child node and new projection if absent.
	 *
	 * Note: This is a short hand version of {@code node(<identity>).projection(<type>)}.
	 *
	 * @param identity  a named or to-stringable object that represent the child node, must not be null
	 * @param type  a projection type representing the known object, must not be null
	 * @param <T>  the known object type
	 * @return the known object matching the child identity and type, never null
	 */
	<T> KnownDomainObject<T> node(Object identity, Class<T> type);

	/**
	 * Returns known object of the specified type and its configuration; creates a new child node and new projection if absent.
	 *
	 * If the configuration action is assertable, the projection will be asserted to match the configuration instead of executing the configuration.
	 * For example, {@code node("runtime", Configuration, declarable())} would assert the configuration of the known object is declarable if the underlying object already exists.
	 * It's useful when different nodes has overlapping projections, e.g. result in the same domain object name.
	 *
	 * Note: this is a short hand version of {@code node(<identity>).projection(<type>, Action)}.
	 *
	 * @param identity  a named or to-stringable object that represent the child node, must not be null
	 * @param type  a projection type representing the known object, must not be null
	 * @param action  a configuration action for the known object, must not be null
	 * @param <T>  the known object type
	 * @return the known object matching the child identity and type, never null
	 */
	<T> KnownDomainObject<T> node(Object identity, Class<T> type, Action<? super T> action);

	/**
	 * Configures child node and its known object of the specified identity and type; creates a new child node and new projection if absent.
	 *
	 * @param identity  a named or to-stringable object that represent the child node, must not be null
	 * @param type  a projection type representing the known object, must not be null
	 * @param action  an action to execute on the child node matching identity and its known object matching type, must not be null
	 * @param <T>  the known object type
	 * @return the known object matching the child identity and type, never null
	 */
	<T> KnownDomainObject<T> node(Object identity, Class<T> type, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> action);

	/** @see #node(Object, Class, BiConsumer) */
	<T> KnownDomainObject<T> node(Object identity, Class<T> type, @ClosureParams(value = FromString.class, options = { "dev.nokee.model.KnownDomainObject<T>", "dev.nokee.model.dsl.ModelNode,dev.nokee.model.KnownDomainObject<T>" }) @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure);

	/**
	 * Returns known object of this node for the specified type; creates a new projection if absent.
	 *
	 * @param type  a projection type representing the known object, must not be null
	 * @param <T>  the known object type
	 * @return the known object of the specified type, never null
	 */
	<T> KnownDomainObject<T> projection(Class<T> type);

	/**
	 * Returns known object of this node for the specified type and its configuration; creates a new projection if absent.
	 *
	 * If the configuration action is assertable, the projection will be asserted to match the configuration of the known object instead of executing the configuration.
	 * For example, {@code projection(Configuration, declarable())} would assert the configuration is declarable if the underlying projection object already exists.
	 * It's useful when different nodes has overlapping projections, e.g. result in the same domain object name.
	 *
	 * @param type  a projection type representing the known object, must not be null
	 * @param action  a configuration action for the known object, must not be null
	 * @param <T>  the known object type
	 * @return the known object of the specified type, never null
	 */
	<T> KnownDomainObject<T> projection(Class<T> type, Action<? super T> action);

	/** @see #projection(Class, Action) */
	default <T> KnownDomainObject<T> projection(Class<T> type, @ClosureParams(FirstParam.FirstGenericType.class) @DelegatesTo(target = "T", strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return projection(type, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Executes action on all objects matching the specified predicate scoped to this node as the root.
	 *
	 * <pre>
	 * nokee.model {
	 *     // Configures component and variant source sets
	 *     main.all(descendants(ofType(LanguageSourceSet))) { node, knownSourceSet -&gt;
	 *         // ...
	 *     }
	 *
	 *     // Configures only component source sets
	 *     main.all(directChildren(ofType(LanguageSourceSet))) { node, knownSourceSet -&gt;
	 *         // ...
	 *     }
	 *
	 *     // Configures all component's known objects
	 *     main.all(descendants()) { node, knownObject -&gt;
	 *     	   // ...
	 *     	   // Note: there may be many invocation for the same node
	 *     }
	 * }
	 * </pre>
	 *
	 * @param predicate  the predicate to match model projection, must not be null
	 * @param action  the action to execute, must not be null
	 * @param <T>  the projection type
	 */
	<T> void all(NodePredicate<? super T> predicate, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> action);

	/**
	 * Executes action on all objects matching the specified predicate scoped to this node as the root.
	 * An instance of the action class is created using the {@link ObjectFactory#newInstance(Class, Object...)}.
	 *
	 * @param predicate  the predicate to match model projection, must not be null
	 * @param rule  the rule class, must not be null
	 * @param <T>  the projection type
	 */
	<T> void all(NodePredicate<T> predicate, Class<? extends BiConsumer<? super ModelNode, ? super KnownDomainObject<T>>> rule);

	/** @see #all(NodePredicate, BiConsumer) */
	<T> void all(NodePredicate<T> predicate, @ClosureParams(value = FromString.class, options = { "dev.nokee.model.KnownDomainObject<T>", "dev.nokee.model.dsl.ModelNode,dev.nokee.model.KnownDomainObject<T>" }) @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure);

	/**
	 * Returns a provider of all objects matching the specified spec for the subgraph with this node as the root.
	 *
	 * To register a configuration on each matching objects, use {@link #all(NodePredicate, BiConsumer)}.
	 *
	 * <pre>
	 * nokee.model {
	 *     // Query all source directories
	 *     Provider&lt;Iterable&lt;FileTree&gt;&gt; sourceFiles = main.all(descendants(ofType(LanguageSourceSet))).map { it*.asFileTree }
	 * }
	 * </pre>
	 *
	 * @param predicate  the predicate to match model projection, must not be null
	 * @param <T>  the objects type
	 * @return a provider of objects matching the predicate, never null
	 */
	<T> Provider<? extends Iterable<T>> all(NodePredicate<T> predicate);
}
