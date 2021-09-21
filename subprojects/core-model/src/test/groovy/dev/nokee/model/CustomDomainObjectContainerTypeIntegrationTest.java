/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model;

import dev.nokee.model.internal.BaseNamedDomainObjectContainer;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;

import javax.inject.Inject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.type.ModelType.of;

class CustomDomainObjectContainerTypeIntegrationTest extends NamedDomainObjectViewTester<CustomDomainObjectContainerTypeIntegrationTest.MyType> {
	@Override
	protected TestContainerGenerator<MyType> getSubjectGenerator() {
		return new TestContainerGenerator<MyType>() {
			private final DefaultModelRegistry modelRegistry = new DefaultModelRegistry(objectFactory()::newInstance);

			@Override
			public DomainObjectContainer<MyType> create(String name) {
				return modelRegistry.register(namedContainer(name, of(CustomContainerOfMyType.class))).get();
			}

			@Override
			public Class<? extends DomainObjectView<MyType>> getSubjectType() {
				return CustomContainerOfMyType.class;
			}

			@Override
			public Class<MyType> getElementType() {
				return MyType.class;
			}

			@Override
			@SuppressWarnings("unchecked")
			public <S extends MyType> Class<S> getSubElementType() {
				return (Class<S>) MyChildType.class;
			}

			@Override
			public ModelRegistry getModelRegistry() {
				return modelRegistry;
			}

			@Override
			public ModelLookup getModelLookup() {
				return modelRegistry;
			}
		};
	}

	interface MyType {}
	interface MyChildType extends MyType {}
	static class CustomContainerOfMyType extends BaseNamedDomainObjectContainer<MyType> {
		@Inject
		public CustomContainerOfMyType(Class<MyType> elementType) {
			super(elementType);
		}
	}
}
