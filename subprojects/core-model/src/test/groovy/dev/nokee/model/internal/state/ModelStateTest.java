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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

class ModelStateTest implements ModelStateTester.None {
	private final ModelNode subject = new ModelNode();

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
	}
}
