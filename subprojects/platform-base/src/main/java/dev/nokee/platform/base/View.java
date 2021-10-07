/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.base;

import groovy.lang.Closure;
import groovy.lang.DelegatesTo;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import java.util.List;
import java.util.Set;

/**
 * A view of a collection that are created and configured as they are required.
 *
 * @param <T> type of the elements in this view
 * @since 0.4
 */
public interface View<T> {
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
	default void configureEach(@DelegatesTo(type = "T", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		configureEach(ConfigureUtil.configureUsing(closure));
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
	default <S extends T> void configureEach(Class<S> type, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		configureEach(type, ConfigureUtil.configureUsing(closure));
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
	default void configureEach(Spec<? super T> spec, @DelegatesTo(type = "S", strategy = Closure.DELEGATE_FIRST) @SuppressWarnings("rawtypes") Closure closure) {
		configureEach(spec, ConfigureUtil.configureUsing(closure));
	}

	/**
	 * Returns a view containing the objects in this view of the given type.
	 * The returned view is live, so that when matching objects are later added to this view, they are also visible in the filtered view.
	 *
	 * @param type the type of element to find.
	 * @param <S> the base type of the element of the new view.
	 * @return the matching element as a {@link View}, never null.
	 */
	<S extends T> View<S> withType(Class<S> type);

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
}
