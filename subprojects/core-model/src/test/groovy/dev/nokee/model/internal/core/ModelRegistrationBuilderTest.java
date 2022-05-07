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

import lombok.val;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.hamcrest.Matchers.notNullValue;

class ModelRegistrationBuilderTest {
	private final ModelRegistration.Builder subject = builder();

	@Test
	void canCreateBuilder() {
		assertThat(subject, notNullValue());
	}

	@Test
	void doesNotModifyComponentsOnBuiltRegistrationViaBuilder() {
		val registration = subject.withComponent(new ModelComponent() {}).build();
		subject.withComponent(new ModelComponent() {}); // no effect on registration
		assertThat(registration.getComponents(), iterableWithSize(1));
	}

	@Test
	void doesNotIncludeDuplicateComponents() {
		val component = new ModelComponent() {};
		val registration = subject.withComponent(component).withComponent(component).build();
		assertThat(registration.getComponents(), contains(component));
	}

	@Test
	void ordersComponentsAccordingToBuilderWithComponentInvocation() {
		val componentA = new ModelComponent() {};
		val componentB = new ModelComponent() {};
		val componentC = new ModelComponent() {};
		val registration = subject.withComponent(componentA).withComponent(componentB).withComponent(componentC).build();
		assertThat(registration.getComponents(), contains(componentA, componentB, componentC));
	}

	@Test
	void canIncludeAnotherModelRegistrationInCurrentBuilder() {
		val componentA = new ModelComponent() {};
		val componentB = new ModelComponent() {};
		val componentC = new ModelComponent() {};
		val registration = subject.withComponent(componentC)
			.mergeFrom(builder().withComponent(componentA).withComponent(componentB).build()).build();
		assertThat(registration.getComponents(), contains(componentC, componentA, componentB));
	}

	@Nested
	class DefaultBuildTest implements ModelRegistrationTester {
		private final ModelRegistration subject = ModelRegistrationBuilderTest.this.subject.build();

		@Override
		public ModelRegistration subject() {
			return subject;
		}

		@Test
		public void hasComponents() {
			assertThat(subject().getComponents(), emptyIterable());
		}
	}

	@Nested
	class WithComponentTest implements ModelRegistrationTester {
		private final ModelComponent component = new ModelComponent() {};
		private final ModelRegistration subject = ModelRegistrationBuilderTest.this.subject.withComponent(component).build();

		@Override
		public ModelRegistration subject() {
			return subject;
		}

		@Test
		public void hasComponents() {
			assertThat(subject().getComponents(), contains(component));
		}
	}
}
