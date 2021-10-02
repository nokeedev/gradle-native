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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ModelStateComponentTest {
	private final ModelNode subject = node();

	@Nested
	class RegisterTest {
		@BeforeEach
		void transitionNodeToRegistered() {
			ModelNodeUtils.register(subject);
		}

		@Test
		void hasRegisteredState() {
			assertThat(subject.getComponent(ModelNode.State.class), equalTo(ModelNode.State.Registered));
		}
	}

	@Nested
	class RealizedTest {
		@BeforeEach
		void transitionNodeToRealized() {
			ModelNodeUtils.realize(subject);
		}

		@Test
		void hasRealizedState() {
			assertThat(subject.getComponent(ModelNode.State.class), equalTo(ModelNode.State.Realized));
		}
	}
}
