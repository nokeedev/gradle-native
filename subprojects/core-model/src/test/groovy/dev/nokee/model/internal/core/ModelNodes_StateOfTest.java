/*
 * Copyright 2020 the original author or authors.
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

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.stateOf;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ModelNodes_StateOfTest {
	@Test
	void checkToString() {
		assertThat(stateOf(ModelNode.State.Registered), hasToString("ModelNodes.stateOf(Registered)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(stateOf(ModelNode.State.Registered), stateOf(ModelNode.State.Registered))
			.addEqualityGroup(stateOf(ModelNode.State.Initialized))
			.addEqualityGroup(stateOf(ModelNode.State.Realized))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeForStateOfInitialized() {
		val predicate = stateOf(ModelNode.State.Initialized);
		assertTrue(predicate.test(node()));
		assertFalse(predicate.test(ModelNodeUtils.register(node())));
		assertFalse(predicate.test(ModelNodeUtils.realize(node())));
	}

	@Test
	void canCreatePredicateFilterForModelNodeForStateOfRegistered() {
		val predicate = stateOf(ModelNode.State.Registered);
		assertFalse(predicate.test(node()));
		assertTrue(predicate.test(ModelNodeUtils.register(node())));
		assertFalse(predicate.test(ModelNodeUtils.realize(node())));
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastRealized() {
		val predicate = stateOf(ModelNode.State.Realized);
		assertFalse(predicate.test(node()));
		assertFalse(predicate.test(ModelNodeUtils.register(node())));
		assertTrue(predicate.test(ModelNodeUtils.realize(node())));
	}
}
