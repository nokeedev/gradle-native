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

import dev.nokee.internal.testing.MockitoUtils;
import dev.nokee.model.internal.core.DefaultComponentRegistry;
import dev.nokee.model.internal.core.ComponentRegistry;
import dev.nokee.model.internal.core.ModelNode;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;
import static org.mockito.Mockito.never;

class ModelStateTest {
	private final ComponentRegistry registry = MockitoUtils.spy(ComponentRegistry.class, new DefaultComponentRegistry());
	private final ModelNode subject = newEntity(registry);

	@Test
	void isAtLeastCreated() {
		assertTrue(ModelState.Created.isAtLeast(ModelState.Created));
		assertTrue(ModelState.Initialized.isAtLeast(ModelState.Created));
		assertTrue(ModelState.Registered.isAtLeast(ModelState.Created));
		assertTrue(ModelState.Realized.isAtLeast(ModelState.Created));
		assertTrue(ModelState.Finalized.isAtLeast(ModelState.Created));
	}

	@Test
	void isAtLeastInitialized() {
		assertFalse(ModelState.Created.isAtLeast(ModelState.Initialized));
		assertTrue(ModelState.Initialized.isAtLeast(ModelState.Initialized));
		assertTrue(ModelState.Registered.isAtLeast(ModelState.Initialized));
		assertTrue(ModelState.Realized.isAtLeast(ModelState.Initialized));
		assertTrue(ModelState.Finalized.isAtLeast(ModelState.Initialized));
	}

	@Test
	void isAtLeastRegistered() {
		assertFalse(ModelState.Created.isAtLeast(ModelState.Registered));
		assertFalse(ModelState.Initialized.isAtLeast(ModelState.Registered));
		assertTrue(ModelState.Registered.isAtLeast(ModelState.Registered));
		assertTrue(ModelState.Realized.isAtLeast(ModelState.Registered));
		assertTrue(ModelState.Finalized.isAtLeast(ModelState.Registered));
	}

	@Test
	void isAtLeastRealized() {
		assertFalse(ModelState.Created.isAtLeast(ModelState.Realized));
		assertFalse(ModelState.Initialized.isAtLeast(ModelState.Realized));
		assertFalse(ModelState.Registered.isAtLeast(ModelState.Realized));
		assertTrue(ModelState.Realized.isAtLeast(ModelState.Realized));
		assertTrue(ModelState.Finalized.isAtLeast(ModelState.Realized));
	}

	@Test
	void isAtLeastFinalized() {
		assertFalse(ModelState.Created.isAtLeast(ModelState.Finalized));
		assertFalse(ModelState.Initialized.isAtLeast(ModelState.Finalized));
		assertFalse(ModelState.Registered.isAtLeast(ModelState.Finalized));
		assertFalse(ModelState.Realized.isAtLeast(ModelState.Finalized));
		assertTrue(ModelState.Finalized.isAtLeast(ModelState.Finalized));
	}

	@Nested
	class NoneTest implements ModelStateTester.None {
		@Override
		public ModelNode subject() {
			return subject;
		}
	}

	@Nested
	class CreateTest implements ModelStateTester.Created {
		@BeforeEach
		void transitionNodeToCreated() {
			ModelStates.create(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}

		@Test
		void changeStateBeforeAddingTag() {
			val inOrder = Mockito.inOrder(registry);
			inOrder.verify(registry).set(eq(subject.getId()), any(), eq(ModelState.Created));
			inOrder.verify(registry).set(eq(subject.getId()), any(), isA(ModelState.IsAtLeastCreated.class));
		}

		@Test
		void doesNotChangeStateWhenCreateMultipleTime() {
			Mockito.reset(registry);
			ModelStates.create(subject);
			Mockito.verify(registry, never()).set(eq(subject.getId()), any(), isA(ModelState.class));
		}
	}

	@Nested
	class InitializeTest implements ModelStateTester.Initialized {
		@BeforeEach
		void transitionNodeToInitialized() {
			ModelStates.initialize(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}

		@Test
		void changeStateBeforeAddingTag() {
			val inOrder = Mockito.inOrder(registry);
			inOrder.verify(registry).set(eq(subject.getId()), any(), eq(ModelState.Initialized));
			inOrder.verify(registry).set(eq(subject.getId()), any(), isA(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		void doesNotChangeStateWhenInitializeMultipleTime() {
			Mockito.reset(registry);
			ModelStates.initialize(subject);
			Mockito.verify(registry, never()).set(eq(subject.getId()), any(), isA(ModelState.class));
		}
	}

	@Nested
	class RegisterTest implements ModelStateTester.Registered {
		@BeforeEach
		void transitionNodeToRegistered() {
			ModelStates.register(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}

		@Test
		void changeStateBeforeAddingTag() {
			val inOrder = Mockito.inOrder(registry);
			inOrder.verify(registry).set(eq(subject.getId()), any(), eq(ModelState.Registered));
			inOrder.verify(registry).set(eq(subject.getId()), any(), isA(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		void doesNotChangeStateWhenRegisterMultipleTime() {
			Mockito.reset(registry);
			ModelStates.register(subject);
			Mockito.verify(registry, never()).set(eq(subject.getId()), any(), isA(ModelState.class));
		}
	}

	@Nested
	class RealizedTest implements ModelStateTester.Realized {
		@BeforeEach
		void transitionNodeToRealized() {
			ModelStates.realize(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}

		@Test
		void changeStateBeforeAddingTag() {
			val inOrder = Mockito.inOrder(registry);
			inOrder.verify(registry).set(eq(subject.getId()), any(), eq(ModelState.Realized));
			inOrder.verify(registry).set(eq(subject.getId()), any(), isA(ModelState.IsAtLeastRealized.class));
		}

		@Test
		void doesNotChangeStateWhenRealizeMultipleTime() {
			Mockito.reset(registry);
			ModelStates.realize(subject);
			Mockito.verify(registry, never()).set(eq(subject.getId()), any(), isA(ModelState.class));
		}
	}

	@Nested
	class FinalizedTest implements ModelStateTester.Finalized {
		@BeforeEach
		void transitionNodeToRealized() {
			ModelStates.finalize(subject);
		}

		@Override
		public ModelNode subject() {
			return subject;
		}

		@Test
		void changeStateBeforeAddingTag() {
			val inOrder = Mockito.inOrder(registry);
			inOrder.verify(registry).set(eq(subject.getId()), any(), eq(ModelState.Finalized));
			inOrder.verify(registry).set(eq(subject.getId()), any(), isA(ModelState.IsAtLeastFinalized.class));
		}

		@Test
		void doesNotChangeStateWhenFinalizeMultipleTime() {
			Mockito.reset(registry);
			ModelStates.finalize(subject);
			Mockito.verify(registry, never()).set(eq(subject.getId()), any(), isA(ModelState.class));
		}
	}
}
