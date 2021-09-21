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

import static dev.nokee.model.internal.core.ModelNodes.withType;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static dev.nokee.model.internal.core.ModelTestUtils.projectionOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_WithTypeTest {
	@Test
	void checkToString() {
		assertThat(withType(of(MyType.class)), hasToString("ModelNodes.withType(interface dev.nokee.model.internal.core.ModelNodes_WithTypeTest$MyType)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(withType(of(MyType.class)), withType(of(MyType.class)))
			.addEqualityGroup(withType(of(WrongType.class)))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByType() {
		val predicate = withType(of(MyType.class));
		assertTrue(predicate.test(node(projectionOf(MyType.class))));
		assertFalse(predicate.test(node(projectionOf(WrongType.class))));
	}

	interface MyType {}
	interface WrongType {}
}
