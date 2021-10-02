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
package dev.nokee.model.internal;

import dev.nokee.model.internal.core.ModelNode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelNodeUtils.getParent;
import static dev.nokee.model.internal.core.ModelNodeUtils.setParent;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ModelNodeParentTest {
	private final ModelNode subject = new ModelNode();

	@Test
	void hasNoParent() {
		assertThat(getParent(subject), emptyOptional());
	}

	@Nested
	class ParentTest {
		private final ModelNode parent = new ModelNode();

		@BeforeEach
		void setUp() {
			setParent(subject, parent);
		}

		@Test
		void hasParent() {
			assertThat(getParent(subject), optionalWithValue(equalTo(parent)));
		}
	}
}
