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
package dev.nokee.model.internal;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.fixtures.ModelRegistryTestUtils.identifier;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@PluginRequirement.Require(id = "dev.nokee.model-base")
class DefaultModelObjectElementQueryIntegrationTest extends AbstractPluginTest {
	private final DomainObjectIdentifier i0 = identifier("a.b");
	private final DomainObjectIdentifier i1 = identifier("a.b");
	private final DomainObjectIdentifier i2 = identifier("a.c");
	private DomainObjectProvider<Object> object;

	@BeforeEach
	void createObject() {
		val registry = project.getExtensions().getByType(ModelRegistry.class);
		object = registry.register(builder().withComponent(new IdentifierComponent(identifier("a"))).withComponent(ofInstance(new Object())).build()).as(Object.class);
		registry.register(builder().withComponent(new IdentifierComponent(i0)).withComponent(ofInstance("peda")).build());
		registry.register(builder().withComponent(new IdentifierComponent(i1)).withComponent(createdUsing(of(MyType.class), () -> objectFactory().newInstance(MyType.class))).build());
		registry.register(builder().withComponent(new IdentifierComponent(i2)).withComponent(ofInstance(Boolean.TRUE)).build());
	}

	@Nested
	class FirstElementTest {
		private DomainObjectProvider<String> subject;

		@BeforeEach
		void createSubject() {
			subject = object.element("b", String.class);
		}

		@Test
		void returnsFirstElementType() {
			assertEquals(String.class, subject.getType());
		}

		@Test
		void returnsFirstElementIdentifier() {
			assertEquals(i0, subject.getIdentifier());
		}
	}

	@Nested
	class SecondElementTest {
		private DomainObjectProvider<MyType> subject;

		@BeforeEach
		void createSubject() {
			subject = object.element("b", MyType.class);
		}

		@Test
		void returnsSecondElementType() {
			assertEquals(MyType.class, subject.getType());
		}

		@Test
		void returnsSecondElementIdentifier() {
			assertEquals(i1, subject.getIdentifier());
		}
	}

	@Nested
	class ThirdElementTest {
		private DomainObjectProvider<Boolean> subject;

		@BeforeEach
		void createSubject() {
			subject = object.element("c").as(Boolean.class);
		}

		@Test
		void returnsThirdElementType() {
			assertEquals(Boolean.class, subject.getType());
		}

		@Test
		void returnsThirdElementIdentifier() {
			assertEquals(i2, subject.getIdentifier());
		}
	}

	@Nested
	class MultipleElementWithSameTypeTest {
		@BeforeEach
		void createConflictingElement() {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			registry.register(builder().withComponent(new IdentifierComponent(identifier("a.b"))).withComponent(ofInstance("vequ")).build());
		}

		@Test
		void throwsExceptionIfMultipleElementHaveTheSameType() {
			assertThrows(RuntimeException.class, () -> object.element("b", String.class));
		}

		@Test
		void doesNotThrowExceptionIfOnlyOneElementHaveTheSpecifiedType() {
			assertDoesNotThrow(() -> object.element("b", MyType.class));
		}

		@Test
		void throwsExceptionIfMultipleElementHaveTheSameName() {
			assertThrows(RuntimeException.class, () -> object.element("b"));
		}

		@Test
		void doesNotThrowExceptionIfOnlyOneElementHaveTheSpecifiedName() {
			assertDoesNotThrow(() -> object.element("c"));
		}
	}

	interface MyType {}
}
