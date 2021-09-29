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
package dev.nokee.model.internal.state;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.type.ModelType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelStateTagTest {
	private final ModelNode subject = node();

	interface ModelStateTester {
		ModelNode subject();
	}

	interface ModelStateCreatedTester extends ModelStateTester {
		@Test
		default void tagsEntityWhenCreated() {
			assertTrue(subject().canBeViewedAs(ModelType.of(ModelState.Created.class)));
		}
	}

	interface ModelStateInitializedTester extends ModelStateTester {
		@Test
		default void tagsEntityWhenInitialized() {
			assertTrue(subject().canBeViewedAs(ModelType.of(ModelState.Initialized.class)));
		}
	}

	interface ModelStateRegisteredTester extends ModelStateTester {
		@Test
		default void tagsEntityWhenRegistered() {
			assertTrue(subject().canBeViewedAs(ModelType.of(ModelState.Registered.class)));
		}
	}

	interface ModelStateRealizedTester extends ModelStateTester {
		@Test
		default void tagsEntityWhenRealized() {
			assertTrue(subject().canBeViewedAs(ModelType.of(ModelState.Realized.class)));
		}
	}

	@Nested
	class RegisterTest implements ModelStateCreatedTester, ModelStateInitializedTester, ModelStateRegisteredTester {
		@BeforeEach
		void transitionNodeToRegistered() {
			ModelNodeUtils.register(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}

		@Test
		void doesNotHaveRealizedTag() {
			assertFalse(subject().canBeViewedAs(ModelType.of(ModelState.Realized.class)));
		}
	}

	@Nested
	class RealizedTest implements ModelStateCreatedTester, ModelStateInitializedTester, ModelStateRegisteredTester, ModelStateRealizedTester {
		@BeforeEach
		void transitionNodeToRealized() {
			ModelNodeUtils.realize(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}
	}
}
