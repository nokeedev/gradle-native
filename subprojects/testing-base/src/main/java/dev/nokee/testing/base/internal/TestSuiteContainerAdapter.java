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
package dev.nokee.testing.base.internal;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelRegistrationFactory;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.model.internal.core.NodeRegistrationFactoryLookup;
import dev.nokee.model.internal.core.RelativeRegistrationService;
import dev.nokee.model.internal.dsl.GroovyDslSupport;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.ViewConfigurationBaseComponent;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.utils.ClosureWrappedConfigureAction;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.type.ModelType.of;

public final class TestSuiteContainerAdapter extends GroovyObjectSupport implements TestSuiteContainer, ModelNodeAware {
	private final ViewAdapter<TestSuiteComponent> delegate;
	private final ModelRegistry registry;
	private final GroovyDslSupport dslSupport;
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();

	@SuppressWarnings("unchecked") // IntelliJ is incompetent
	public TestSuiteContainerAdapter(ViewAdapter<TestSuiteComponent> delegate, ModelRegistry registry) {
		this.delegate = delegate;
		this.registry = registry;
		dslSupport = GroovyDslSupport.builder()
			.metaClass(getMetaClass())
			.whenGetProperty(this::find)
			.whenInvokeMethod(Class.class, this::register)
			.whenInvokeMethod(Class.class, Closure.class, this::register)
			.build();
	}

	private Optional<Object> find(String name) {
		try {
			return Optional.of(delegate.named(name));
		} catch (Throwable ex) {
			return Optional.empty();
		}
	}

	@Override
	public <U extends TestSuiteComponent> NamedDomainObjectProvider<U> register(String name, Class<U> type) {
		return register(name, of(type));
	}

	@Override
	public <U extends TestSuiteComponent> NamedDomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action) {
		val result = register(name, of(type));
		result.configure(action);
		return result;
	}

	public <U extends TestSuiteComponent> NamedDomainObjectProvider<U> register(String name, Class<U> type, @SuppressWarnings("rawtypes") Closure closure) {
		val result = register(name, of(type));
		result.configure(new ClosureWrappedConfigureAction<>(closure));
		return result;
	}

	private <U extends TestSuiteComponent> NamedDomainObjectProvider<U> register(String name, ModelType<U> type) {
		val registrationFactoryLookup = ModelNodeUtils.get(entity, NodeRegistrationFactoryLookup.class);
		if (registrationFactoryLookup.getSupportedTypes().contains(type)) {
			val factory = registrationFactoryLookup.get(type);
			if (factory instanceof NodeRegistrationFactory) {
				return ModelNodeUtils.register(entity, ((NodeRegistrationFactory) factory).create(name)).as(type).asProvider();
			} else {
				return entity.get(RelativeRegistrationService.class).modelRegistry.register(((ModelRegistrationFactory) factory).create(name)).as(type).asProvider();
			}
		}
		val identifier = ComponentIdentifier.of(name, (ProjectIdentifier) entity.get(ViewConfigurationBaseComponent.class).get().get(IdentifierComponent.class).get());
		return registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(identifier)).withComponent(managed(type)).build()).as(type).asProvider();
	}

	@Override
	public NamedDomainObjectProvider<TestSuiteComponent> named(String name) {
		return delegate.named(name);
	}


	@Override
	public void configureEach(Action<? super TestSuiteComponent> action) {
		delegate.configureEach(action);
	}

	@Override
	public void configureEach(@SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(closure);
	}

	@Override
	public <S extends TestSuiteComponent> void configureEach(Class<S> type, Action<? super S> action) {
		delegate.configureEach(type, action);
	}

	@Override
	public <S extends TestSuiteComponent> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(type, closure);
	}

	@Override
	public void configureEach(Spec<? super TestSuiteComponent> spec, Action<? super TestSuiteComponent> action) {
		delegate.configureEach(spec, action);
	}

	@Override
	public void configureEach(Spec<? super TestSuiteComponent> spec, @SuppressWarnings("rawtypes") Closure closure) {
		delegate.configureEach(spec, closure);
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
	public Object getProperty(String propertyName) {
		return dslSupport.getProperty(propertyName);
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		return dslSupport.invokeMethod(name, args);
	}

	@Override
	public ModelNode getNode() {
		return entity;
	}
}
