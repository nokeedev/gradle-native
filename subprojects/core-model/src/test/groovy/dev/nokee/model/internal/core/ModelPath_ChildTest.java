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

import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelPath.path;
import static dev.nokee.model.internal.core.ModelPath.root;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelPath_ChildTest {
	@Test
	void canCreateChildPath() {
		assertEquals(path("a.b.c"), path("a.b").child("c"), "child path should be the same as an absolute path");
	}

	@Test
	void canGetParentOfChildPath() {
		assertThat(path("a.b").child("c").getParent(), optionalWithValue(equalTo(path("a.b"))));
	}

	@Test
	void canCreateChildPathFromRootPath() {
		assertEquals(path("a"), root().child("a"), "child path from root should be the same as an absolute path");
	}
}
