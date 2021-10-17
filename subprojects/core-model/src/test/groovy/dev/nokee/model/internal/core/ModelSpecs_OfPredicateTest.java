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
import org.junit.jupiter.api.Test;

import static com.google.common.base.Predicates.alwaysTrue;
import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static dev.nokee.model.internal.core.ModelNodes.withPath;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelSpecs.of;
import static dev.nokee.model.internal.core.ModelTestUtils.node;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModelSpecs_OfPredicateTest {
	private final ModelSpec subject = of(withPath(path("po.ta.to")));

	@Test
	void noOpinionOnPath() {
		assertThat(subject.getPath(), emptyOptional());
	}

	@Test
	void noOpinionOnParent() {
		assertThat(subject.getParent(), emptyOptional());
	}

	@Test
	void noOpinionOnAncestor() {
		assertThat(subject.getAncestor(), emptyOptional());
	}

	@Test
	void checkIsSatisfiedBy() {
		assertTrue(subject.isSatisfiedBy(node("po.ta.to")));
		assertFalse(subject.isSatisfiedBy(node("po.ta")));
		assertFalse(subject.isSatisfiedBy(node("po.ta.to.yeah")));
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("ModelSpecs.of(ModelNodes.withPath(po.ta.to))"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of(alwaysTrue()), of(alwaysTrue()))
			.addEqualityGroup(of(withPath(path("po.ta.to"))))
			.testEquals();
	}
}
