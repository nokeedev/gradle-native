package dev.nokee.model;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import java.util.List;
import java.util.Set;

import static org.gradle.util.ConfigureUtil.configureUsing;

/**
 * A view of a collection that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 * @since 0.5
 */
// TODO: Think about splitting the default implementation away from the interface and introduce
//   AbstractDomainObjectView -> AbstractNamedDomainObjectView -> AbstractDomainObjectContainer (or just move away from those construct)
//   Then have a BaseDomainObjectView, BaseNamedDomainObjectView and BaseDomainObjectContainer for implementer
//   Each of those concrete class should pull the ModelNode from a ThreadLocal implementation (a-la BaseLanguageSourceSet)
//   This should allow us to use final implementation for View and container
//   However we need to change, yet again, the implementation for GroovySupport.... we can get around it for now.
// TODO: We should introduce NodeInitializer, NodeDiscover, NodeConfigurer... maybe to follow a certain lifecycle
//   Initializer can add projection and further discover/configuer
//   Discover can add more node and further configurer
public interface DomainObjectView<T> {
	/**
	 * Registers an action to execute to configure each element in the view.
	 * The action is only executed for those elements that are required.
	 * Fails if any element has already been finalized.
	 *
	 * @param action The action to execute on each element for configuration.
	 */
	void configureEach(Action<? super T> action);

	/**
	 * Registers a closure to execute to configure each element in the view.
	 * The action is only executed for those elements that are required.
	 * Fails if any element has already been finalized.
	 *
	 * @param closure The closure to execute on each element for configuration.
	 */
	default void configureEach(@DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configureEach(configureUsing(closure));
	}

	/**
	 * Registers an action to execute to configure each element in the view.
	 * The action is only executed for those elements that are required.
	 * Fails if any matching element has already been finalized.
	 *
	 * This method is equivalent to <code>view.withType(Foo).configureEach { ... }</code>.
	 *
	 * @param type the type of binary to select.
	 * @param <S> the base type of the element to configure.
	 * @param action the action to execute on each element for configuration.
	 */
	<S extends T> void configureEach(Class<S> type, Action<? super S> action);

	/**
	 * Registers a closure to execute to configure each element in the view.
	 * The action is only executed for those elements that are required.
	 * Fails if any matching element has already been finalized.
	 *
	 * This method is equivalent to <code>view.withType(Foo).configureEach { ... }</code>.
	 *
	 * @param type the type of binary to select.
	 * @param <S> the base type of the element to configure.
	 * @param closure the closure to execute on each element for configuration.
	 */
	default <S extends T> void configureEach(Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configureEach(type, configureUsing(closure));
	}

	/**
	 * Registers an action to execute to configure each element in the view matching the given specification.
	 * The action is only executed for those elements that are required.
	 * Fails if any element has already been finalized.
	 *
	 * @param spec a specification to satisfy. The spec is applied to each binary prior to configuration.
	 * @param action the action to execute on each element for configuration.
	 * @since 0.4
	 */
	void configureEach(Spec<? super T> spec, Action<? super T> action);

	/**
	 * Registers a closure to execute to configure each element in the view matching the given specification.
	 * The action is only executed for those elements that are required.
	 * Fails if any element has already been finalized.
	 *
	 * @param spec a specification to satisfy. The spec is applied to each binary prior to configuration.
	 * @param closure the closure to execute on each element for configuration.
	 * @since 0.4
	 */
	default void configureEach(Spec<? super T> spec, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		configureEach(spec, configureUsing(closure));
	}

	/**
	 * Returns the contents of this view as a {@link Provider} of {@code <T>} instances.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @return a provider containing all the elements included in this view.
	 */
	Provider<Set<T>> getElements();

	/**
	 * Returns the contents of this view as a {@link Set} of {@code <T>} instances.
	 *
	 * @return a set containing all the elements included in this view.
	 */
	Set<T> get();

	/**
	 * Returns a list containing the results of applying the given mapper function to each element in the view.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @param mapper a transform function to apply on each element of this view
	 * @param <S> the type of the mapped elements
	 * @return a provider containing the transformed elements included in this view.
	 */
	<S> Provider<List<S>> map(Transformer<? extends S, ? super T> mapper);

	/**
	 * Returns a single list containing all elements yielded from results of mapper function being invoked on each element of this view.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @param mapper a transform function to apply on each element of this view
	 * @param <S> the type of the mapped elements
	 * @return a provider containing the mapped elements of this view.
	 */
	<S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super T> mapper);

	/**
	 * Returns a single list containing all elements matching the given specification.
	 *
	 * <p>The returned {@link Provider} is live, and tracks changes of the view.</p>
	 *
	 * @param spec a specification to satisfy
	 * @return a provider containing the filtered elements of this view.
	 */
	Provider<List<T>> filter(Spec<? super T> spec);

	/**
	 * Executes given action when an element becomes known to the view.
	 *
	 * @param action  the action to execute for each known element of this view
	 */
	// TODO: Rename to whenElementKnown once all views have been migrated
	default void whenElementKnownEx(Action<? super KnownDomainObject<T>> action) {}

	/**
	 * Executes given closure when an element becomes known to the view of the specified type.
	 *
	 * @param closure  the closure to execute for each known element of this view
	 */
	// TODO: Rename to whenElementKnown once all views have been migrated
	default void whenElementKnownEx(@DelegatesTo(value = KnownDomainObject.class, strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnownEx(ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Executes given action when an element becomes known to the view of the specified type.
	 *
	 * @param type  the element type to match
	 * @param action  the action to execute for each known element of this view
	 * @param <S>  the element type to match
	 */
	// TODO: Rename to whenElementKnown once all views have been migrated
	default <S extends T> void whenElementKnownEx(Class<S> type, Action<? super KnownDomainObject<S>> action) {}

	/**
	 * Executes given closure when an element becomes known to the view of the specified type.
	 *
	 * @param type  the element type to match
	 * @param closure  the closure to execute for each known element of this view
	 * @param <S>  the element type to match
	 */
	// TODO: Rename to whenElementKnown once all views have been migrated
	default <S extends T> void whenElementKnownEx(Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) Closure<Void> closure) {
		whenElementKnownEx(type, ConfigureUtil.configureUsing(closure));
	}
}
