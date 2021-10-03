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
import dev.nokee.model.internal.state.ModelState;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.stateAtLeast;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.*;

public class ModelNodes_StateAtLeastTest {
	@Test
	void checkToString() {
		assertThat(stateAtLeast(ModelState.Registered), hasToString("ModelNodes.stateAtLeast(Registered)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(stateAtLeast(ModelState.Registered), stateAtLeast(ModelState.Registered))
			.addEqualityGroup(stateAtLeast(ModelState.Initialized))
			.addEqualityGroup(stateAtLeast(ModelState.Realized))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastInitialized() {
		val predicate = stateAtLeast(ModelState.Initialized);
		assertAll(() -> {
			assertTrue(predicate.test(node()));
			assertTrue(predicate.test(ModelNodeUtils.register(node())));
			assertTrue(predicate.test(ModelNodeUtils.realize(node())));
		});
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastRegistered() {
		val predicate = stateAtLeast(ModelState.Registered);
		assertAll(() -> {
			assertFalse(predicate.test(node()));
			assertTrue(predicate.test(ModelNodeUtils.register(node())));
			assertTrue(predicate.test(ModelNodeUtils.realize(node())));
		});
	}

	@Test
	void canCreatePredicateFilterForModelNodeByStateAtLeastRealized() {
		val predicate = stateAtLeast(ModelState.Realized);
		assertAll(() -> {
			assertFalse(predicate.test(node()));
			assertFalse(predicate.test(ModelNodeUtils.register(node())));
			assertTrue(predicate.test(ModelNodeUtils.realize(node())));
		});
	}
}
