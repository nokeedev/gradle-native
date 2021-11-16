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
package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.jvm.GroovySourceSet;
import dev.nokee.language.jvm.JavaSourceSet;
import dev.nokee.language.jvm.KotlinSourceSet;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.HasName;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.registry.ModelBackedNamedDomainObjectProvider;
import dev.nokee.platform.base.SourceView;
import dev.nokee.platform.jni.JavaNativeInterfaceLibrarySources;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import java.util.List;
import java.util.Set;

public final class JavaNativeInterfaceSourcesViewAdapter implements JavaNativeInterfaceLibrarySources {
	private final SourceView<LanguageSourceSet> delegate;

	public JavaNativeInterfaceSourcesViewAdapter(SourceView<LanguageSourceSet> delegate) {
		this.delegate = delegate;
	}

	public NamedDomainObjectProvider<GroovySourceSet> getGroovy() {
		val result = ModelProperties.findProperty(this, "groovy");
		if (result.isPresent()) {
			return new ModelBackedNamedDomainObjectProvider<>(result.get().as(GroovySourceSet.class));
		}
		throw new RuntimeException("Please apply 'groovy' plugin to access Groovy source set.");
	}

	public void groovy(Action<? super GroovySourceSet> action) {
		getGroovy().configure(action);
	}

	public void groovy(@SuppressWarnings("rawtypes") Closure closure) {
		groovy(ConfigureUtil.configureUsing(closure));
	}

	public NamedDomainObjectProvider<JavaSourceSet> getJava() {
		val result = ModelProperties.findProperty(this, "java");
		if (result.isPresent()) {
			return new ModelBackedNamedDomainObjectProvider<>(result.get().as(JavaSourceSet.class));
		}
		throw new RuntimeException("Please apply 'java' plugin to access Java source set.");
	}

	public void java(Action<? super JavaSourceSet> action) {
		getJava().configure(action);
	}

	public void java(@SuppressWarnings("rawtypes") Closure closure) {
		java(ConfigureUtil.configureUsing(closure));
	}

	public NamedDomainObjectProvider<KotlinSourceSet> getKotlin() {
		val result = ModelProperties.findProperty(this, "kotlin");
		if (result.isPresent()) {
			return new ModelBackedNamedDomainObjectProvider<>(result.get().as(KotlinSourceSet.class));
		}
		throw new RuntimeException("Please apply 'org.jetbrains.kotlin.jvm' plugin to access Kotlin source set.");
	}

	public void kotlin(Action<? super KotlinSourceSet> action) {
		getKotlin().configure(action);
	}

	public void kotlin(@SuppressWarnings("rawtypes") Closure closure) {
		kotlin(ConfigureUtil.configureUsing(closure));
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
		throw new UnsupportedOperationException();
	}

	@Override
	public void whenElementKnownEx(@SuppressWarnings("rawtypes") Closure closure) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends LanguageSourceSet> void whenElementKnownEx(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends LanguageSourceSet> void whenElementKnownEx(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		throw new UnsupportedOperationException();
	}

	@Override
	public void configure(String name, Action<? super LanguageSourceSet> action) {
		configureEach(it -> {
			if (ModelNodes.of(it).getComponent(HasName.class).getName().toString().equals(name)) {
				action.execute(it);
			}
		});
	}

	@Override
	public void configure(String name, @SuppressWarnings("rawtypes") Closure closure) {
		configureEach(it -> {
			if (ModelNodes.of(it).getComponent(HasName.class).getName().toString().equals(name)) {
				ConfigureUtil.configureUsing(closure).execute(it);
			}
		});
	}

	@Override
	public <S extends LanguageSourceSet> void configure(String name, Class<S> type, Action<? super S> action) {
		configureEach(type, it -> {
			if (ModelNodes.of(it).getComponent(HasName.class).getName().toString().equals(name)) {
				action.execute(it);
			}
		});
	}

	@Override
	public <S extends LanguageSourceSet> void configure(String name, Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		configureEach(type, it -> {
			if (ModelNodes.of(it).getComponent(HasName.class).getName().toString().equals(name)) {
				ConfigureUtil.configureUsing(closure).execute(it);
			}
		});
	}

	@Override
	public DomainObjectProvider<LanguageSourceSet> get(String name) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends LanguageSourceSet> DomainObjectProvider<S> get(String name, Class<S> type) {
		throw new UnsupportedOperationException();
	}

	@Override
	public <S extends LanguageSourceSet> NamedDomainObjectProvider<S> named(String name, Class<S> type) {
		throw new UnsupportedOperationException();
	}
}
