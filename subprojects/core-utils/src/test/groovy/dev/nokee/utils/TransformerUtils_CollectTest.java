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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static com.google.common.collect.ImmutableList.toImmutableList;
import static com.google.common.collect.ImmutableSet.toImmutableSet;
import static dev.nokee.utils.TransformerUtils.collect;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TransformerUtils_CollectTest {
	@Test
	void canCollectIterable() {
		assertThat(collect(joining("-")).transform(Arrays.asList("foo", "bar")), equalTo("foo-bar"));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(collect(toList()), isA(TransformerUtils.Transformer.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(collect(toImmutableList()), collect(toImmutableList()))
			.addEqualityGroup(collect(toImmutableSet()))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(collect(toImmutableList()), hasToString(startsWith("TransformerUtils.collect(")));
	}
}
