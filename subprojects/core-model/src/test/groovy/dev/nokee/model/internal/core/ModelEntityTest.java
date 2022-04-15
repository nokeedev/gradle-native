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
package dev.nokee.model.internal.core;

import lombok.Value;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelEntityTest {
	private final ModelNode subject = new ModelNode();

	@Test
	void hasId() {
		assertThat(subject.getId(), notNullValue(ModelEntityId.class));
	}

	@Test
	void hasNoComponentsOnNewEntity() {
		assertThat(subject.getComponents().collect(toList()), emptyIterable());
	}

	@Nested
	class ExistingComponentTest {
		private final Object existingComponent = new ModelEntityTest.TestComponent("foo");
		private final ModelNodeListener listener = Mockito.mock(ModelNodeListener.class);

		@BeforeEach
		void addComponent() {
			subject.addComponent(existingComponent);
			subject.addComponent(new ModelNodeListenerComponent(listener));
			Mockito.reset(listener);
		}

		@Test
		void hasComponent() {
			assertAll(
				() -> assertTrue(subject.has(ModelEntityTest.TestComponent.class)),
				() -> assertThat(subject.getComponents().collect(toList()), hasItem(existingComponent))
			);
		}

		@Test
		void replaceComponentWhenAddingExistingComponent() {
			val newComponent = new ModelEntityTest.TestComponent("bar");
			subject.addComponent(newComponent);
			assertThat(subject.getComponents().collect(toList()), allOf(hasItem(newComponent), not(hasItem(existingComponent))));
			Mockito.verify(listener).projectionAdded(subject, newComponent);
		}

		@Test
		void doesNotReplaceComponentWhenAddingSameComponent() {
			subject.addComponent(existingComponent);
			assertThat(subject.getComponents().collect(toList()), hasItem(existingComponent));
			Mockito.verifyNoInteractions(listener);
		}

		@Test
		void doesNotReplaceComponentWhenAddingEqualComponent() {
			val newComponent = new ModelEntityTest.TestComponent("foo");
			subject.addComponent(newComponent);
			assertThat(subject.getComponents().collect(toList()), hasItem(existingComponent));
			Mockito.verifyNoInteractions(listener);
		}
	}

	@Value
	private static class TestComponent implements ModelComponent {
		String value;
	}
}
