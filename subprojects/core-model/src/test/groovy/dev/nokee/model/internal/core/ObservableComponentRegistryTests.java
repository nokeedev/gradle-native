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
package dev.nokee.model.internal.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.internal.testing.forwarding.ForwardingWrapperEx.forwarding;
import static dev.nokee.internal.testing.forwarding.ForwardingWrapperMatchers.forwardsToDelegate;
import static dev.nokee.internal.testing.reflect.MethodInformation.method;
import static dev.nokee.model.fixtures.ModelEntityTestUtils.aComponentId;
import static dev.nokee.model.fixtures.ModelEntityTestUtils.newComponentRegistry;
import static org.hamcrest.MatcherAssert.assertThat;

class ObservableComponentRegistryTests {
	@Nested
	class ListenerNotifyTests {
		ModelEntityId entityId = ModelEntityId.nextId();
		ObservableComponentRegistry.Listener listener = Mockito.mock(ObservableComponentRegistry.Listener.class);
		ModelComponentRegistry subject = new ObservableComponentRegistry(newComponentRegistry(), listener);

		Component.Id componentId = aComponentId();
		ModelComponent component = Mockito.mock(ModelComponent.class);

		@BeforeEach
		void givenNewComponent() {
			subject.set(entityId, componentId, component);
		}

		@Test
		void notifyListener() {
			Mockito.verify(listener).componentChanged(entityId, componentId, component);
		}

		@Nested
		class WhenReplacingComponentWithSameComponent {
			@BeforeEach
			void givenReplaceWithSameComponent() {
				Mockito.reset(listener);
				subject.set(entityId, componentId, component);
			}

			@Test
			void doesNotNotifyListener() {
				Mockito.verifyNoInteractions(listener);
			}
		}

		@Nested
		class WhenReplacingComponentWithDifferentComponent {
			ModelComponent differentComponent = Mockito.mock(ModelComponent.class);

			@BeforeEach
			void givenReplaceWithNewComponent() {
				Mockito.reset(listener);
				subject.set(entityId, componentId, differentComponent);
			}

			@Test
			void notifyListener() {
				Mockito.verify(listener).componentChanged(entityId, componentId, differentComponent);
			}
		}
	}

	@Nested
	class ForwardingTests {
		@Test
		void forwardsSetToDelegate() {
			assertThat(forwarding(ModelComponentRegistry.class, this::newWrapper),
				forwardsToDelegate(method(ModelComponentRegistry::set)));
		}

		@Test
		void forwardsGetToDelegate() {
			assertThat(forwarding(ModelComponentRegistry.class, this::newWrapper),
				forwardsToDelegate(method(ModelComponentRegistry::get)));
		}

		@Test
		void forwardsGetAllIdsToDelegate() {
			assertThat(forwarding(ModelComponentRegistry.class, this::newWrapper),
				forwardsToDelegate(method(ModelComponentRegistry::getAllIds)));
		}

		@Test
		void forwardsGetAllToDelegate() {
			assertThat(forwarding(ModelComponentRegistry.class, this::newWrapper),
				forwardsToDelegate(method(ModelComponentRegistry::getAll)));
		}

		private ModelComponentRegistry newWrapper(ModelComponentRegistry delegate) {
			return new ObservableComponentRegistry(delegate, noOpListener());
		}
	}

	private static ObservableComponentRegistry.Listener noOpListener() {
		return new ObservableComponentRegistry.Listener() {
			@Override
			public void componentChanged(ModelEntityId entityId, Component.Id componentId, ModelComponent component) {
				// do nothing
			}
		};
	}
}
