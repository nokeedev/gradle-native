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
package dev.nokee.platform.ios.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.ObjectiveCIosApplicationSources;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public final class ObjectiveCIosApplicationSourcesAdapter implements ObjectiveCIosApplicationSources {
	private final ViewAdapter<LanguageSourceSet> delegate;

	public ObjectiveCIosApplicationSourcesAdapter(ViewAdapter<LanguageSourceSet> delegate) {
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super LanguageSourceSet> action) {
		delegate.configureEach(action);
	}

	@Override
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(closure);
	}

	@Override
	public <S extends LanguageSourceSet> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(type, action);
	}

	@Override
	public <S extends LanguageSourceSet> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super LanguageSourceSet> spec, Action<? super LanguageSourceSet> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public void configureEach(Spec<? super LanguageSourceSet> spec, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(spec, closure);
	}

	@Override
	public Provider<Set<LanguageSourceSet>> getElements() {
		return delegate.getElements();
	}

	@Override
	public Set<LanguageSourceSet> get() {
		return delegate.get();
	}

	@Override
	public <S> Provider<List<S>> map(Transformer<? extends S, ? super LanguageSourceSet> mapper) {
		return delegate.map(mapper);
	}

	@Override
	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super LanguageSourceSet> mapper) {
		return delegate.flatMap(mapper);
	}

	@Override
	public Provider<List<LanguageSourceSet>> filter(Spec<? super LanguageSourceSet> spec) {
		return delegate.filter(spec);
	}

	@Override
	public void whenElementKnown(Action<? super KnownDomainObject<LanguageSourceSet>> action) {
		delegate.whenElementKnown(action);
	}

	@Override
	public void whenElementKnown(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.whenElementKnown(closure);
	}

	@Override
	public <S extends LanguageSourceSet> void whenElementKnown(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		delegate.whenElementKnown(type, action);
	}

	@Override
	public <S extends LanguageSourceSet> void whenElementKnown(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.whenElementKnown(type, closure);
	}

	@Override
	public void configure(String name, Action<? super LanguageSourceSet> action) {
		delegate.named(name, action);
	}

	@Override
	public void configure(String name, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.named(name, closure);
	}

	@Override
	public <S extends LanguageSourceSet> void configure(String name, Class<S> type, Action<? super S> action) {
		delegate.named(name, type, action);
	}

	@Override
	public <S extends LanguageSourceSet> void configure(String name, Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.named(name, type, closure);
	}

	@Override
	public <S extends LanguageSourceSet> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
		return delegate.named(name, type);
	}

	@Override
	public <S extends LanguageSourceSet> View<S> withType(Class<S> type) {
		return delegate.withType(type);
	}

	@Override
	public NamedDomainObjectProvider<IosResourceSet> getResources() {
		return named("resources", IosResourceSet.class);
	}

	@Override
	public void resources(Action<? super IosResourceSet> action) {
		delegate.named("resources", IosResourceSet.class, action);
	}

	@Override
	public void resources(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.named("resources", IosResourceSet.class, closure);
	}
}
