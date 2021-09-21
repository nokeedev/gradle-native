/*
 * Copyright 2020-2021 the original author or authors.
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
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.core.ModelNodes.isSatisfiedByProjection;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.SpecUtils.satisfyAll;
import static dev.nokee.utils.SpecUtils.satisfyNone;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_SatisfiedByProjectionTest {
	@Test
	void checkToString() {
		assertThat(isSatisfiedByProjection(of(MyType.class), satisfyAll()),
			hasToString("ModelNodes.isSatisfiedByProjection(interface dev.nokee.model.internal.core.ModelNodes_SatisfiedByProjectionTest$MyType, SpecUtils.satisfyAll())"));
	}

	@Test
	void returnsTheValueFromTheSpec() {
		val node = node(projectionOf(MyType.class));
		assertTrue(isSatisfiedByProjection(of(MyType.class), satisfyAll()).test(node));
		assertFalse(isSatisfiedByProjection(of(MyType.class), satisfyNone()).test(node));
	}

	@Test
	void returnsFalseWhenNodeHasNoProjection() {
		assertFalse(isSatisfiedByProjection(of(MyType.class), satisfyAll()).test(node(projectionOf(WrongType.class))));
	}

	interface MyType {}
	interface WrongType {}
}
