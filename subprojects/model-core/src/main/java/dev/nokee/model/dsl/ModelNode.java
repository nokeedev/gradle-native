package dev.nokee.model.dsl;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.ClosureBackedBiAction;
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
 * <pre>
 * plugins {
 *     id 'dev.nokee.model-base'
 * }
 *
 * nokee.model {
 *     def compileTask = test.compile(Exec) { // will register Exec task named 'compileTest'
 *         // executed when
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
public interface ModelNode extends Named {
	/**
	 * Return model node matching the specified identity or create new node if absent.
	 * @param identity  a named or to-stringable object that represent the child node, must not be null
	 * @return
	 */
	ModelNode node(Object identity);
	ModelNode node(Object identity, Action<? super ModelNode> action);
	default ModelNode node(Object identity, @ClosureParams(value = SimpleType.class, options = "dev.nokee.model.dsl.ModelNode") @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return node(identity, ConfigureUtil.configureUsing(closure));
	}

	<T> KnownDomainObject<T> node(Object identity, Class<T> type);
	<T> KnownDomainObject<T> node(Object identity, Class<T> type, BiConsumer<? super ModelNode, ? super KnownDomainObject<T>> action);
	default <T> KnownDomainObject<T> node(Object identity, Class<T> type, @ClosureParams(value = FromString.class, options = { "dev.nokee.model.KnownDomainObject<T>", "dev.nokee.model.dsl.ModelNode,dev.nokee.model.KnownDomainObject<T>" }) @DelegatesTo(value = ModelNode.class, strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return node(identity, type, new ClosureBackedBiAction<>(closure));
	}
	<T> KnownDomainObject<T> projection(Class<T> type);
	<T> KnownDomainObject<T> projection(Class<T> type, Action<? super T> action); // TODO: action here should be passed to the registerIfAbsent for asserting existing object
	default <T> KnownDomainObject<T> projection(Class<T> type, @ClosureParams(FirstParam.FirstGenericType.class) @DelegatesTo(target = "T", strategy = Closure.DELEGATE_FIRST) Closure<?> closure) {
		return projection(type, ConfigureUtil.configureUsing(closure));
	}
}
