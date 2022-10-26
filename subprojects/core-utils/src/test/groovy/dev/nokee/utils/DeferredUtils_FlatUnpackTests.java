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
import static dev.nokee.utils.DeferredUtils.flatUnpack;
import static dev.nokee.utils.DeferredUtils_BaseSpec.callableOf;
import static dev.nokee.utils.DeferredUtils_BaseSpec.closureOf;
import static dev.nokee.utils.DeferredUtils_BaseSpec.kotlinFunctionOf;
import static dev.nokee.utils.DeferredUtils_BaseSpec.supplierOf;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class DeferredUtils_FlatUnpackTests {
	@Test
	void returnsEmptyListWhenFlatUnpackingNull() {
		assertThat(flatUnpack((Object) null), emptyIterable());
	}

	@Test
	void canFlatUnpackAlreadyFlattenAndUnpackedList() {
		assertThat(flatUnpack(of("a", "b", "c")), contains("a", "b", "c"));
	}

	@Test
	void canFlatUnpackAnAlreadyUnpackedList() {
		assertThat(flatUnpack(of(of("a", "b"), of("c", of("d")))), contains("a", "b", "c", "d"));
	}

	@Test
	void canUnpackMixedSinglePackedValues() {
		assertThat(flatUnpack(of(callableOf("a"), supplierOf("b"), closureOf("c"), kotlinFunctionOf("d"))),
			contains("a", "b", "c", "d"));
	}

	@Test
	void canUnpackMixedMultiPackedValues() {
		assertThat(flatUnpack(of(callableOf(of("a1", "a2")), supplierOf(of("b1", "b2")), closureOf(of("c1", "c2")), kotlinFunctionOf(of("d1", "d2")))), contains("a1", "a2", "b1", "b2", "c1", "c2", "d1", "d2"));
	}

	@Test
	void canUnpackListContainingEmptyList() {
		assertThat(flatUnpack(of(emptyList())), emptyIterable());
	}
}
