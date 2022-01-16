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
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import lombok.EqualsAndHashCode;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Objects;
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
		Objects.requireNonNull(action);
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

	public void whenElementKnown(Action<? super KnownDomainObject<T>> action) {
		strategy.whenElementKnown(elementType, action);
	}

	public void whenElementKnown(@SuppressWarnings("rawtypes") Closure closure) {
		strategy.whenElementKnown(elementType, new ClosureWrappedConfigureAction<>(closure));
	}

	public <S extends T> void whenElementKnown(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		strategy.whenElementKnown(type, action);
	}

	public <S extends T> void whenElementKnown(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		strategy.whenElementKnown(type, new ClosureWrappedConfigureAction<>(closure));
	}

	public NamedDomainObjectProvider<T> named(String name) {
		return strategy.named(name, elementType);
	}

	public NamedDomainObjectProvider<T> named(String name, Action<? super T> action) {
		val result = strategy.named(name, elementType);
		result.configure(action);
		return result;
	}

	public NamedDomainObjectProvider<T> named(String name, @SuppressWarnings("rawtypes") Closure closure) {
		val result = strategy.named(name, elementType);
		result.configure(new ClosureWrappedConfigureAction<>(closure));
		return result;
	}

	public <S extends T> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
		return strategy.named(name, type);
	}

	public <S extends T> NamedDomainObjectProvider<S> named(String name, Class<S> type, Action<? super S> action) {
		val result = strategy.named(name, type);
		result.configure(action);
		return result;
	}

	public <S extends T> NamedDomainObjectProvider<S> named(String name, Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		val result = strategy.named(name, type);
		result.configure(new ClosureWrappedConfigureAction<>(closure));
		return result;
	}

	public interface Strategy {
		<T> void configureEach(Class<T> elementType, Action<? super T> action);
		<T> Provider<Set<T>> getElements(Class<T> elementType);
		<T> void whenElementKnown(Class<T> elementType, Action<? super KnownDomainObject<T>> action);
		<T> NamedDomainObjectProvider<T> named(String name, Class<T> elementType);
	}
}
