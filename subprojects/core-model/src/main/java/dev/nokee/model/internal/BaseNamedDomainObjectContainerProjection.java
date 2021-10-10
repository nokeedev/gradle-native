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
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactoryLookup;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.Action;

import java.util.HashMap;
import java.util.Map;

import static dev.nokee.model.internal.core.ModelNodeContext.getCurrentModelNode;

public class BaseNamedDomainObjectContainerProjection implements AbstractModelNodeBackedNamedDomainObjectContainer.Projection {
	private final PolymorphicDomainObjectInstantiator<Object> instantiator = new AbstractPolymorphicDomainObjectInstantiator<Object>(Object.class, "") {};
	private final Map<Class<?>, Class<?>> bindings = new HashMap<>();
	private final ModelNode node = getCurrentModelNode();

	@SuppressWarnings("unchecked")
	private <U> Class<U> toImplementationType(Class<U> type) {
		return (Class<U>) bindings.getOrDefault(type, type);
	}

	@Override
	public <U> DomainObjectProvider<U> register(String name, ModelType<U> type) {
		val registrationFactoryLookup = ModelNodeUtils.get(node, NodeRegistrationFactoryLookup.class);
		if (registrationFactoryLookup.getSupportedTypes().contains(type)) {
			return ModelNodeUtils.register(node, registrationFactoryLookup.get(type).create(name)).as(type);
		}
		if (instantiator.getCreatableTypes().contains(type.getRawType())) {
			return ModelNodeUtils.register(node, NodeRegistration.unmanaged(name, ModelType.of(toImplementationType(type.getConcreteType())), () -> instantiator.newInstance(new NameAwareDomainObjectIdentifier() {
				@Override
				public Object getName() {
					return name;
				}
			}, toImplementationType(type.getConcreteType())))).as(type);
		}
		return ModelNodeUtils.register(node, NodeRegistration.of(name, type)).as(type);
	}

	@Override
	public <U> DomainObjectProvider<U> register(String name, ModelType<U> type, Action<? super U> action) {
		val provider = register(name, type);
		provider.configure(action);
		return provider;
	}

	@Override
	public <U> void registerFactory(Class<U> type, DomainObjectFactory<? extends U> factory) {
		instantiator.registerFactory(type, factory);
	}

	@Override
	public <U> void registerBinding(Class<U> type, Class<? extends U> implementationType) {
		instantiator.registerBinding(type, implementationType);
		bindings.put(type, implementationType);
	}
}
