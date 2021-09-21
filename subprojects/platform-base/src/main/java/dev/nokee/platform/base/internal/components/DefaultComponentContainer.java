/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.internal.components;

import com.google.common.base.Preconditions;
import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.BaseNamedDomainObjectContainer;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import lombok.val;
import org.gradle.api.Action;

import javax.inject.Inject;

public class DefaultComponentContainer extends BaseNamedDomainObjectContainer<Component> implements ComponentContainer {
	@Inject
	public DefaultComponentContainer() {
		super(Component.class);
	}

	@Override
	public <U extends Component> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot register test suite component types in this container, use a TestSuiteContainer instead.");
		super.registerFactory(type, factory);
	}

	@Override
	public <U extends Component> void registerBinding(Class<U> type, Class<? extends U> implementationType) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot bind test suite component types in this container, use a TestSuiteContainer instead.");
		Preconditions.checkArgument(!isTestSuiteComponent(implementationType), "Cannot bind to test suite component types in this container, use a TestSuiteContainer instead.");
		super.registerBinding(type, implementationType);
	}

	@Override
	public <S extends Component> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
		super.configureEach(type, action);
	}

	@Override
	public <S extends Component> void whenElementKnownEx(Class<S> type, Action<? super KnownDomainObject<S>> action) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
		super.whenElementKnownEx(type, action);
	}

	@Override
	public <U extends Component> DomainObjectProvider<U> register(String name, Class<U> type) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot register test suite components in this container, use a TestSuiteContainer instead.");
		return super.register(name, type);
	}

	@Override
	public <U extends Component> DomainObjectProvider<U> register(String name, Class<U> type, Action<? super U> action) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot register test suite components in this container, use a TestSuiteContainer instead.");
		return super.register(name, type, action);
	}

	@Override
	public <S extends Component> void configure(String name, Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(!isTestSuiteComponent(type), "Cannot configure test suite components in this container, use a TestSuiteContainer instead.");
		super.configure(name, type, action);
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
