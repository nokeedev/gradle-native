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
import lombok.val;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isA;

class ModelStateTest implements ModelStateTester.None {
	private final ModelNode subject = new ModelNode();
	private final ModelNodeListener listener = Mockito.mock(ModelNodeListener.class);

	@BeforeEach
	void setUpListener() {
		subject.addComponent(listener);
	}

	@Override
	public ModelNode subject() {
		return subject;
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
	}
}
