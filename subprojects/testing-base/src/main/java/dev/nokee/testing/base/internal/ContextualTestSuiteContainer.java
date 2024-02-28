/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.testing.base.internal;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;
import java.util.List;
import java.util.Set;

public /*final*/ abstract class ContextualTestSuiteContainer implements TestSuiteContainer {
	private final ModelObjectIdentifier baseIdentifier;
	private final ModelObjectRegistry<TestSuiteComponent> testSuiteRegistry;
	private final ViewAdapter<TestSuiteComponent> delegate;

	@Inject
	public ContextualTestSuiteContainer(ModelObjectIdentifier baseIdentifier, ModelObjectRegistry<TestSuiteComponent> testSuiteRegistry, ViewAdapter<TestSuiteComponent> delegate) {
		this.baseIdentifier = baseIdentifier;
		this.testSuiteRegistry = testSuiteRegistry;
		this.delegate = delegate;
	}

	@Override
	public void configureEach(Action<? super TestSuiteComponent> action) {
		delegate.configureEach(action);
	}

	@Override
	public <S> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(type, action);
	}

	@Override
	public void configureEach(Spec<? super TestSuiteComponent> spec, Action<? super TestSuiteComponent> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public <S extends TestSuiteComponent> View<S> withType(Class<S> type) {
		return delegate.withType(type);
	}

	@Override
	public Provider<Set<TestSuiteComponent>> getElements() {
		return delegate.getElements();
	}

	@Override
	public Set<TestSuiteComponent> get() {
		return delegate.get();
	}

	@Override
	public <S> Provider<List<S>> map(Transformer<? extends S, ? super TestSuiteComponent> mapper) {
		return delegate.map(mapper);
	}

	@Override
	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super TestSuiteComponent> mapper) {
		return delegate.flatMap(mapper);
	}

	@Override
	public Provider<List<TestSuiteComponent>> filter(Spec<? super TestSuiteComponent> spec) {
		return delegate.filter(spec);
	}

	@Override
	public <T extends TestSuiteComponent> NamedDomainObjectProvider<T> register(String name, Class<T> type) {
		return testSuiteRegistry.register(baseIdentifier.child(name), type).asProvider();
	}
}
