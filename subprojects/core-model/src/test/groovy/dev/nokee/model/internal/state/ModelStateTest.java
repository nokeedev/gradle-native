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
import dev.nokee.model.internal.core.ModelNodeListener;
import dev.nokee.model.internal.core.ModelNodeListenerComponent;
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;

class ModelStateTest {
	private final ModelNode subject = new ModelNode();
	private final ModelNodeListener listener = Mockito.mock(ModelNodeListener.class);

	@BeforeEach
	void setUpListener() {
		subject.addComponent(new ModelNodeListenerComponent(listener));
	}

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
			val inOrder = Mockito.inOrder(listener);
			inOrder.verify(listener).projectionAdded(subject, ModelState.Created);
			inOrder.verify(listener).projectionAdded(eq(subject), isA(ModelState.IsAtLeastCreated.class));
		}

		@Test
		void doesNotChangeStateWhenCreateMultipleTime() {
			Mockito.reset(listener);
			ModelStates.create(subject);
			Mockito.verifyNoInteractions(listener);
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
			val inOrder = Mockito.inOrder(listener);
			inOrder.verify(listener).projectionAdded(subject, ModelState.Initialized);
			inOrder.verify(listener).projectionAdded(eq(subject), isA(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		void doesNotChangeStateWhenInitializeMultipleTime() {
			Mockito.reset(listener);
			ModelStates.initialize(subject);
			Mockito.verifyNoInteractions(listener);
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
			val inOrder = Mockito.inOrder(listener);
			inOrder.verify(listener).projectionAdded(subject, ModelState.Registered);
			inOrder.verify(listener).projectionAdded(eq(subject), isA(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		void doesNotChangeStateWhenRegisterMultipleTime() {
			Mockito.reset(listener);
			ModelStates.register(subject);
			Mockito.verifyNoInteractions(listener);
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
			val inOrder = Mockito.inOrder(listener);
			inOrder.verify(listener).projectionAdded(subject, ModelState.Realized);
			inOrder.verify(listener).projectionAdded(eq(subject), isA(ModelState.IsAtLeastRealized.class));
		}

		@Test
		void doesNotChangeStateWhenRealizeMultipleTime() {
			Mockito.reset(listener);
			ModelStates.realize(subject);
			Mockito.verifyNoInteractions(listener);
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
			val inOrder = Mockito.inOrder(listener);
			inOrder.verify(listener).projectionAdded(subject, ModelState.Finalized);
			inOrder.verify(listener).projectionAdded(eq(subject), isA(ModelState.IsAtLeastFinalized.class));
		}

		@Test
		void doesNotChangeStateWhenFinalizeMultipleTime() {
			Mockito.reset(listener);
			ModelStates.finalize(subject);
			Mockito.verifyNoInteractions(listener);
		}
	}
}
