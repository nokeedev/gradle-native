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
import org.gradle.nativeplatform.test.TestComponent;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelEntityTest {
	private final ModelNode subject = new ModelNode();

	@Test
	void hasNoComponents() {
		assertThat(subject.getComponents().collect(toList()), emptyIterable());
	}

	@Nested
	class ExistingComponentTest {
		private final Object existingComponent = new ModelEntityTest.TestComponent("foo");
		@BeforeEach
		void addComponent() {
			subject.addComponent(existingComponent);
		}

		@Test
		void hasComponent() {
			assertAll(
				() -> assertTrue(subject.hasComponent(ModelEntityTest.TestComponent.class)),
				() -> assertThat(subject.getComponents().collect(toList()), hasItem(existingComponent))
			);
		}

		@Test
		void replaceComponentWhenAddingExistingComponent() {
			val newComponent = new ModelEntityTest.TestComponent("bar");
			subject.addComponent(newComponent);
			assertThat(subject.getComponents().collect(toList()), allOf(hasItem(newComponent), not(hasItem(existingComponent))));
		}
	}

	@Value
	private static class TestComponent {
		String value;
	}
}
