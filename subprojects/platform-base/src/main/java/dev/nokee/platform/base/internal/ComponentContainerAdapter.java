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

import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.ComponentSpec;
import dev.nokee.platform.base.View;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;
import java.util.function.BiFunction;

public final class ComponentContainerAdapter implements ComponentContainer {
	private final View<Component> delegate;
	private final BiFunction<String, Class<? extends ComponentSpec>, ModelElement> registry;

	public ComponentContainerAdapter(View<Component> delegate, BiFunction<String, Class<? extends ComponentSpec>, ModelElement> registry) {
		this.delegate = delegate;
		this.registry = registry;
	}

	@Override
	public ModelElement register(String name, Class<? extends ComponentSpec> type) {
		return registry.apply(name, type);
	}

	@Override
	public void configureEach(Action<? super Component> action) {
		delegate.configureEach(action);
	}

	@Override
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(closure);
	}

	@Override
	public <S extends Component> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(type, action);
	}

	@Override
	public <S extends Component> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super Component> spec, Action<? super Component> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public void configureEach(Spec<? super Component> spec, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(spec, closure);
	}

	@Override
	public <S extends Component> View<S> withType(Class<S> type) {
		return delegate.withType(type);
	}

	@Override
	public Provider<Set<Component>> getElements() {
		return delegate.getElements();
	}

	@Override
	public Set<Component> get() {
		return delegate.get();
	}

	@Override
	public <S> Provider<List<S>> map(Transformer<? extends S, ? super Component> mapper) {
		return delegate.map(mapper);
	}

	@Override
	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super Component> mapper) {
		return delegate.flatMap(mapper);
	}

	@Override
	public Provider<List<Component>> filter(Spec<? super Component> spec) {
		return delegate.filter(spec);
	}
}
