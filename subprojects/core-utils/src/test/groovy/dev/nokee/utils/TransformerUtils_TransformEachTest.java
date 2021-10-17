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

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TransformerUtils_TransformEachTest {
	@Test
	void canTransformElementsOneToOne() {
		assertThat(transformEach(prefixWith("a-")).transform(of("bar", "foo", "far")),
			contains("a-bar", "a-foo", "a-far"));
	}

	@Test
	void canTransformElementsToAnotherType() {
		assertThat(transformEach(String::length).transform(of("bar", "foobar", "foobarfar")),
			contains(3, 6, 9));
	}

	@Test
	void canTransformSet() {
		assertThat(transformEach(prefixWith("b-")).transform(ImmutableSet.of("bar", "foo", "far")),
			contains("b-bar", "b-foo", "b-far"));
	}

	@Test
	void returnsEmptyListForEmptyInput() {
		assertThat(transformEach(aTransformer()).transform(emptyList()), emptyIterable());
		assertThat(transformEach(aTransformer()).transform(emptySet()),	emptyIterable());
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(transformEach(aTransformer()), isA(TransformerUtils.Transformer.class));
	}

	@Test
	void returnsNoOpTransformerWhenMapperIsNoOp() {
		assertThat(transformEach(noOpTransformer()), equalTo(noOpTransformer()));
	}

	@Test
	void canUseTransformEachFromIterable() {
		Transformer<Iterable<Integer>, Iterable<? extends Integer>> transformer1 = transformEach(aTransformer());
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<Iterable<Integer>, Iterable<Integer>> transformer2 = transformEach(anotherTransformer());
		assertThat(transformer2.transform(ImmutableSet.of(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseTransformEachFromList() {
		Transformer<Iterable<Integer>, List<? extends Integer>> transformer1 = transformEach(aTransformer());
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<Iterable<Integer>, List<Integer>> transformer2 = transformEach(anotherTransformer());
		assertThat(transformer2.transform(asList(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseTransformEachFromSet() {
		Transformer<Iterable<Integer>, Set<? extends Integer>> transformer1 = transformEach(aTransformer());
		assertThat(transformer1.transform(ImmutableSet.of(1, 2, 3)), iterableWithSize(3));

		Transformer<Iterable<Integer>, Set<Integer>> transformer2 = transformEach(anotherTransformer());
		assertThat(transformer2.transform(ImmutableSet.of(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void checkToString() {
		assertThat(transformEach(aTransformer()),
			hasToString("TransformerUtils.transformEach(aTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(transformEach(aTransformer()), transformEach(aTransformer()))
			.addEqualityGroup(transformEach(anotherTransformer()))
			.testEquals();
	}

	private static Transformer<String, String> prefixWith(String prefix) {
		return s -> prefix + s;
	}
}
