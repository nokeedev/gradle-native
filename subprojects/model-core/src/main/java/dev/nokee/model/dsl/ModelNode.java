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
//	<T> KnownDomainObject<T> node(Object identity, Class<T> type, Consumer<? super T> action);

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
	 * Returns known object of this node for the specified type and configuration; creates a new projection if absent.
	 *
	 * If the configuration action is assertable, the projection will be asserted to match the configuration instead of executing the configuration.
	 * For example, {@code projection(Configuration, declarable())} would assert the configuration is declarable if the underlying projection object already exists.
	 * It's useful when different nodes has overlapping projections, e.g. result in the same domain object name.
	 *
	 * @param type  a projection type representing the known object, must not be null
	 * @param action  a configuration action for the known object, must not be null
	 * @param <T>  the known object type
	 * @return the known object of the specified type, never null
	 */
	<T> KnownDomainObject<T> projection(Class<T> type, Action<? super T> action); // TODO: action here should be passed to the registerIfAbsent for asserting existing object

	/** @see #projection(Class, Action) */
	default <T> KnownDomainObject<T> projection(Class<T> type, @ClosureParams(FirstParam.FirstGenericType.class) @DelegatesTo(target = "T", strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return projection(type, ConfigureUtil.configureUsing(closure));
	}
}
