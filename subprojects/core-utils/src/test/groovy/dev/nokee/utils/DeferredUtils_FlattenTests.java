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
package dev.nokee.utils;

import org.junit.jupiter.api.Test;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.DeferredUtils.flatten;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class DeferredUtils_FlattenTests {
	@Test
	void returnsEmptyListWhenFlatUnpackingNull() {
		assertThat(flatten(null), emptyIterable());
	}

	@Test
	void canFlatUnpackAlreadyFlattenAndUnpackedList() {
		assertThat(flatten(of("a", "b", "c")), contains("a", "b", "c"));
	}

	@Test
	void canFlatUnpackAnAlreadyUnpackedList() {
		assertThat(flatten(of(of("a", "b"), of("c", of("d")))), contains("a", "b", "c", "d"));
	}

	@Test
	void canUnpackListContainingEmptyList() {
		assertThat(flatten(of(emptyList())), emptyIterable());
	}
}
