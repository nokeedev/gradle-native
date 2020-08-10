package dev.nokee.platform.base;

import org.gradle.api.Action;
import org.gradle.api.specs.Spec;

public interface DomainObjectElementConfigurer<T> {
	/**
	 * Registers an action to execute to configure each element in the view.
	 * The action is only executed for those elements that are required.
	 * Fails if any element has already been finalized.
	 *
	 * @param action The action to execute on each element for configuration.
	 */
	void configureEach(Action<? super T> action);

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

	<S extends T> void configureEach(Class<S> type, Spec<? super S> spec, Action<? super S> action);

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
}
