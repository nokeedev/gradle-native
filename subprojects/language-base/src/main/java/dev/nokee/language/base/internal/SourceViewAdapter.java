/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.language.base.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.ViewAdapter;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public /*final*/ class SourceViewAdapter<T extends LanguageSourceSet> implements SourceView<T> {
	private final View<T> delegate;

	public SourceViewAdapter(View<T> delegate) {
		this.delegate = delegate;
	}

	@Override
	public <S extends T> SourceView<S> withType(Class<S> type) {
		return new SourceViewAdapter<>(delegate.withType(type));
	}

	@Override
	public void configureEach(Action<? super T> action) {
		delegate.configureEach(action);
	}

	@Override
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(closure);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(type, action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(spec, closure);
	}

	@Override
	public Provider<Set<T>> getElements() {
		return delegate.getElements();
	}

	@Override
	public Set<T> get() {
		return delegate.get();
	}

	@Override
	public <S> Provider<List<S>> map(Transformer<? extends S, ? super T> mapper) {
		return delegate.map(mapper);
	}

	@Override
	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super T> mapper) {
		return delegate.flatMap(mapper);
	}

	@Override
	public Provider<List<T>> filter(Spec<? super T> spec) {
		return delegate.filter(spec);
	}

	public <S extends T> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
		return ((ViewAdapter<T>) delegate).named(name, type);
	}
}
