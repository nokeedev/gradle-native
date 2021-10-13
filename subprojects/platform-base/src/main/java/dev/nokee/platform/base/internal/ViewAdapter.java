/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.model.KnownDomainObject;
import dev.nokee.platform.base.View;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

import static dev.nokee.utils.TransformerUtils.*;

@EqualsAndHashCode
public final class ViewAdapter<T> implements View<T> {
	private final Class<T> elementType;
	private final Strategy strategy;

	public ViewAdapter(Class<T> elementType, Strategy strategy) {
		this.elementType = elementType;
		this.strategy = strategy;
	}

	@Override
	public void configureEach(Action<? super T> action) {
		strategy.configureEach(elementType, action);
	}

	@Override
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		strategy.configureEach(elementType, new ClosureWrappedConfigureAction<>(closure));
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		strategy.configureEach(type, action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		strategy.configureEach(type, new ClosureWrappedConfigureAction<>(closure));
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		strategy.configureEach(elementType, new SpecFilteringAction<>(spec, action));
	}

	@Override
	public void configureEach(Spec<? super T> spec, @SuppressWarnings("rawtypes") Closure closure) {
		strategy.configureEach(elementType, new SpecFilteringAction<>(spec, new ClosureWrappedConfigureAction<>(closure)));
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		return new ViewAdapter<>(type, strategy);
	}

	@Override
	public Provider<Set<T>> getElements() {
		return strategy.getElements(elementType);
	}

	@Override
	public Set<T> get() {
		return getElements().get();
	}

	@Override
	public <S> Provider<List<S>> map(Transformer<? extends S, ? super T> mapper) {
		return getElements().map(transformEach(mapper).andThen(toListTransformer()));
	}

	@Override
	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super T> mapper) {
		return getElements().map(flatTransformEach(mapper).andThen(toListTransformer()));
	}

	@Override
	public Provider<List<T>> filter(Spec<? super T> spec) {
		return getElements().map(matching(spec).andThen(toListTransformer(elementType)));
	}

	public interface Strategy {
		<T> void configureEach(Class<T> elementType, Action<? super T> action);
		<T> Provider<Set<T>> getElements(Class<T> elementType);
		<T> void whenElementKnown(Class<T> elementType, Action<? super KnownDomainObject<T>> action);
	}
}
