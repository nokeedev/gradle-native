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
package dev.nokee.platform.cpp.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.cpp.CppSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.cpp.CppApplicationSources;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public final class CppApplicationSourcesAdapter implements CppApplicationSources {
	private final ViewAdapter<LanguageSourceSet> delegate;

	public CppApplicationSourcesAdapter(ViewAdapter<LanguageSourceSet> delegate) {
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
	public void whenElementKnownEx(Action<? super KnownDomainObject<LanguageSourceSet>> action) {
		delegate.whenElementKnown(action);
	}

	@Override
	public void whenElementKnownEx(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.whenElementKnown(closure);
	}

	@Override
	public <S extends LanguageSourceSet> void whenElementKnownEx(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		delegate.whenElementKnown(type, action);
	}

	@Override
	public <S extends LanguageSourceSet> void whenElementKnownEx(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
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
	public DomainObjectProvider<LanguageSourceSet> get(String name) {
		throw new UnsupportedOperationException("Use named(String) instead.");
	}

	@Override
	public <S extends LanguageSourceSet> DomainObjectProvider<S> get(String name, Class<S> type) {
		throw new UnsupportedOperationException("Use named(String, Class) instead.");
	}

	@Override
	public <S extends LanguageSourceSet> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
		return delegate.named(name, type);
	}

	@Override
	public NamedDomainObjectProvider<CppSourceSet> getCpp() {
		return named("cpp", CppSourceSet.class);
	}

	@Override
	public void cpp(Action<? super CppSourceSet> action) {
		delegate.named("cpp", CppSourceSet.class, action);
	}

	@Override
	public void cpp(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.named("cpp", CppSourceSet.class, closure);
	}

	@Override
	public NamedDomainObjectProvider<NativeHeaderSet> getHeaders() {
		return named("headers", NativeHeaderSet.class);
	}

	@Override
	public void headers(Action<? super NativeHeaderSet> action) {
		delegate.named("headers", NativeHeaderSet.class, action);
	}

	@Override
	public void headers(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.named("headers", NativeHeaderSet.class, closure);
	}
}
