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
package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.dsl.GroovyDslSupport;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import dev.nokee.platform.base.View;
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

public class ComponentContainerAdapter extends GroovyObjectSupport implements ComponentContainer {
	private final View<Component> delegate;
	private final GroovyDslSupport dslSupport;
	private final ModelRegistry registry;
	private final ModelNode entity;

	public ComponentContainerAdapter(View<Component> delegate, ModelRegistry registry) {
		this(delegate, registry, ModelNodeContext.getCurrentModelNode());
	}

	@SuppressWarnings("unchecked") // IntelliJ is incompetent
	public ComponentContainerAdapter(View<Component> delegate, ModelRegistry registry, ModelNode entity) {
		this.delegate = delegate;
		this.registry = registry;
		this.entity = entity;
		dslSupport = GroovyDslSupport.builder()
			.metaClass(getMetaClass())
			.whenGetProperty(this::find)
			.whenInvokeMethod(Class.class, this::register)
			.build();
	}

	private Optional<Object> find(String name) {
		try {
			return Optional.of(((ViewAdapter<Component>) delegate).named(name));
		} catch (Throwable ex) {
			return Optional.empty();
		}
	}

	@Override
	public <U extends Component> NamedDomainObjectProvider<U> register(String name, Class<U> type) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot register test suite components in this container, use a TestSuiteContainer instead.");
		val identifier = ComponentIdentifier.of(name, (ProjectIdentifier) entity.get(ViewConfigurationBaseComponent.class).get().get(IdentifierComponent.class).get());
		return registry.register(ModelRegistration.builder().withComponent(new IdentifierComponent(identifier)).withComponent(managed(of(type))).build()).as(type).asProvider();
	}

	@Override
	public NamedDomainObjectProvider<Component> named(String name) {
		return ((ViewAdapter<Component>) delegate).named(name);
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
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
		delegate.configureEach(type, action);
	}

	@Override
	public <S extends Component> void configureEach(Class<S> type, @SuppressWarnings("rawtypes") Closure closure) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
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
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot filter test suite components in this container, use a TestSuiteContainer instead.");
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

	@Override
	public Object getProperty(String propertyName) {
		return dslSupport.getProperty(propertyName);
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		return dslSupport.invokeMethod(name, args);
	}

	private static boolean isTestSuiteComponent(Class<? extends Component> entityType) {
		try {
			val TestSuiteComponent = Class.forName("dev.nokee.testing.base.TestSuiteComponent");
			return TestSuiteComponent.isAssignableFrom(entityType);
		} catch (ClassNotFoundException e) {
			return false;
		}
	}
}
