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

import static com.google.common.base.Predicates.alwaysFalse;
import static com.google.common.base.Predicates.alwaysTrue;
import static dev.nokee.model.internal.core.ModelNodes.and;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelNodes_AndTest {
	@Test
	void checkToString() {
		assertThat(and(alwaysTrue(), alwaysFalse()), hasToString("ModelNodes.and(Predicates.alwaysTrue(), Predicates.alwaysFalse())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(and(alwaysTrue(), alwaysFalse()), and(alwaysTrue(), alwaysFalse()))
			.addEqualityGroup(and(alwaysTrue(), alwaysTrue()))
			.addEqualityGroup(and(alwaysFalse(), alwaysTrue()))
			.testEquals();
	}

	@Test
	void canCreatePredicateFilterForModelNodeByParent() {
		val predicate = and(it -> ModelNodeUtils.getPath(it).get().endsWith("a"), it -> ModelNodeUtils.getPath(it).get().startsWith("foo"));
		assertTrue(predicate.test(node("foo.a")));
		assertFalse(predicate.test(node("bar.a")));
		assertFalse(predicate.test(node("foo.b")));
	}
}
