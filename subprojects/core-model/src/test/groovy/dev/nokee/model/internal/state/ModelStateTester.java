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
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.state.ModelStates.getState;
import static dev.nokee.model.internal.state.ModelStates.isAtLeast;
import static org.junit.jupiter.api.Assertions.*;

public interface ModelStateTester {
	ModelNode subject();

	interface None extends ModelStateTester {
		@Test
		default void doesNotHaveCreatedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastCreated.class));
		}

		@Test
		default void doesNotHaveInitializedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		default void doesNotHaveRegisteredTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		default void doesNotHaveRealizedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRealized.class));
		}

		@Test
		default void defaultsToCreatedState() {
			assertEquals(getState(subject()), ModelState.Created);
		}

		@Test
		default void doesNotHaveStateComponent() {
			assertFalse(subject().has(ModelState.class));
		}

		@Test
		default void isAtMostCreated() {
			assertTrue(isAtLeast(subject(), ModelState.Created));

			assertFalse(isAtLeast(subject(), ModelState.Initialized));
			assertFalse(isAtLeast(subject(), ModelState.Registered));
			assertFalse(isAtLeast(subject(), ModelState.Realized));
			assertFalse(isAtLeast(subject(), ModelState.Finalized));
		}
	}

	interface Created extends ModelStateTester {
		@Test
		default void hasCreatedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastCreated.class));
		}

		@Test
		default void doesNotHaveInitializedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		default void doesNotHaveRegisteredTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		default void doesNotHaveRealizedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRealized.class));
		}

		@Test
		default void hasCreatedState() {
			assertEquals(getState(subject()), ModelState.Created);
		}

		@Test
		default void hasStateComponent() {
			assertTrue(subject().has(ModelState.class));
		}

		@Test
		default void isAtMostCreated() {
			assertTrue(isAtLeast(subject(), ModelState.Created));

			assertFalse(isAtLeast(subject(), ModelState.Initialized));
			assertFalse(isAtLeast(subject(), ModelState.Registered));
			assertFalse(isAtLeast(subject(), ModelState.Realized));
			assertFalse(isAtLeast(subject(), ModelState.Finalized));
		}
	}

	interface Initialized extends ModelStateTester {
		@Test
		default void hasCreatedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastCreated.class));
		}

		@Test
		default void hasInitializedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		default void doesNotHaveRegisteredTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		default void doesNotHaveRealizedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRealized.class));
		}

		@Test
		default void hasInitializedState() {
			assertEquals(getState(subject()), ModelState.Initialized);
		}

		@Test
		default void hasStateComponent() {
			assertTrue(subject().has(ModelState.class));
		}

		@Test
		default void isAtMostInitialized() {
			assertTrue(isAtLeast(subject(), ModelState.Created));
			assertTrue(isAtLeast(subject(), ModelState.Initialized));

			assertFalse(isAtLeast(subject(), ModelState.Registered));
			assertFalse(isAtLeast(subject(), ModelState.Realized));
			assertFalse(isAtLeast(subject(), ModelState.Finalized));
		}
	}

	interface Registered extends ModelStateTester {
		@Test
		default void hasCreatedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastCreated.class));
		}

		@Test
		default void hasInitializedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		default void hasRegisteredTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		default void doesNotHaveRealizedTag() {
			assertFalse(subject().has(ModelState.IsAtLeastRealized.class));
		}

		@Test
		default void hasRegisteredState() {
			assertEquals(getState(subject()), ModelState.Registered);
		}

		@Test
		default void hasStateComponent() {
			assertTrue(subject().has(ModelState.class));
		}

		@Test
		default void isAtMostRegistered() {
			assertTrue(isAtLeast(subject(), ModelState.Created));
			assertTrue(isAtLeast(subject(), ModelState.Initialized));
			assertTrue(isAtLeast(subject(), ModelState.Registered));

			assertFalse(isAtLeast(subject(), ModelState.Realized));
			assertFalse(isAtLeast(subject(), ModelState.Finalized));
		}
	}

	interface Realized extends ModelStateTester {
		@Test
		default void hasCreatedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastCreated.class));
		}

		@Test
		default void hasInitializedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		default void hasRegisteredTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		default void hasRealizedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastRealized.class));
		}

		@Test
		default void hasRealizedState() {
			assertEquals(getState(subject()), ModelState.Realized);
		}

		@Test
		default void hasStateComponent() {
			assertTrue(subject().has(ModelState.class));
		}

		@Test
		default void isAtMostRealized() {
			assertTrue(isAtLeast(subject(), ModelState.Created));
			assertTrue(isAtLeast(subject(), ModelState.Initialized));
			assertTrue(isAtLeast(subject(), ModelState.Registered));
			assertTrue(isAtLeast(subject(), ModelState.Realized));

			assertFalse(isAtLeast(subject(), ModelState.Finalized));
		}
	}

	interface Finalized extends ModelStateTester {
		@Test
		default void hasCreatedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastCreated.class));
		}

		@Test
		default void hasInitializedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastInitialized.class));
		}

		@Test
		default void hasRegisteredTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastRegistered.class));
		}

		@Test
		default void hasRealizedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastRealized.class));
		}

		@Test
		default void hasFinalizedTagComponent() {
			assertTrue(subject().has(ModelState.IsAtLeastFinalized.class));
		}

		@Test
		default void hasRealizedState() {
			assertEquals(getState(subject()), ModelState.Finalized);
		}

		@Test
		default void hasStateComponent() {
			assertTrue(subject().has(ModelState.class));
		}

		@Test
		default void isAtMostFinalized() {
			assertTrue(isAtLeast(subject(), ModelState.Created));
			assertTrue(isAtLeast(subject(), ModelState.Initialized));
			assertTrue(isAtLeast(subject(), ModelState.Registered));
			assertTrue(isAtLeast(subject(), ModelState.Realized));
			assertTrue(isAtLeast(subject(), ModelState.Finalized));
		}
	}
}
