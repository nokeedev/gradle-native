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
package dev.nokee.model.internal.core;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class DefaultModelElementElementQueryIntegrationTest extends AbstractPluginTest {
	private final DomainObjectIdentifier i0 = mock(DomainObjectIdentifier.class);
	private final DomainObjectIdentifier i1 = mock(DomainObjectIdentifier.class);
	private ModelElement element;

	@BeforeEach
	void createObject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		element = registry.register(builder().withComponent(path("a")).withComponent(ofInstance(new Object())).build());
		registry.register(builder().withComponent(path("a.b")).withComponent(i0).withComponent(createdUsing(of(MyType.class), () -> objectFactory().newInstance(MyType.class))).build());
		registry.register(builder().withComponent(path("a.b")).withComponent(i1).withComponent(ofInstance("dalo")).build());
	}


	@Nested
	class FirstElementTest {
		private DomainObjectProvider<MyType> subject;

		@BeforeEach
		void createSubject() {
			subject = element.element("b", MyType.class);
		}

		@Test
		void returnsFirstElementType() {
			assertEquals(MyType.class, subject.getType());
		}

		@Test
		void returnsFirstElementIdentifier() {
			assertEquals(i0, subject.getIdentifier());
		}
	}

	@Nested
	class SecondElementTest {
		private DomainObjectProvider<String> subject;

		@BeforeEach
		void createSubject() {
			subject = element.element("b", String.class);
		}

		@Test
		void returnsSecondElementType() {
			assertEquals(String.class, subject.getType());
		}

		@Test
		void returnsSecondElementIdentifier() {
			assertEquals(i1, subject.getIdentifier());
		}
	}

	@Nested
	class MultipleElementWithSameTypeTest {
		@BeforeEach
		void createConflictingElement() {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.register(builder().withComponent(path("a.b")).withComponent(mock(DomainObjectIdentifier.class)).withComponent(ofInstance("cere")).build());
		}

		@Test
		void throwsExceptionIfMultipleElementHaveTheSameType() {
			assertThrows(RuntimeException.class, () -> element.element("b", String.class));
		}

		@Test
		void doesNotThrowExceptionIfOnlyOneElementHaveTheSpecifiedType() {
			assertDoesNotThrow(() -> element.element("b", MyType.class));
		}
	}

	interface MyType {}
}
