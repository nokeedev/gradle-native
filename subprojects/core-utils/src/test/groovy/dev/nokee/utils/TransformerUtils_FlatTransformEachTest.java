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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerUtils.flatTransformEach;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Subject(TransformerUtils.class)
class TransformerUtils_FlatTransformEachTest {
	@Test
	void canTransformElementsOneToOne() {
		assertThat(flatTransformEach(prefixWith("a-")).transform(of("bar", "foo", "far")),
			contains("a-bar", "a-foo", "a-far"));
	}

	@Test
	void canTransformElementsOneToMultiple() {
		assertThat(flatTransformEach(it -> of(it + "-1", it + "-2")).transform(of("bar", "foo", "far")),
			contains("bar-1", "bar-2", "foo-1", "foo-2", "far-1", "far-2"));
	}

	@Test
	void canTransformElementsOneToNone() {
		assertThat(flatTransformEach(onlyIf(not("foo"::equals))).transform(of("bar", "foo", "far")),
			contains("bar", "far"));
	}

	@Test
	void canTransformElementsToAnotherType() {
		assertThat(flatTransformEach(length()).transform(of("bar", "foobar", "foobarfar")),
			contains(3, 6, 9));
	}

	@Test
	void canTransformSet() {
		assertThat(flatTransformEach(prefixWith("b-")).transform(ImmutableSet.of("bar", "foo", "far")),
			contains("b-bar", "b-foo", "b-far"));
	}

	@Test
	void returnsEmptyListForEmptyInput() {
		assertThat(flatTransformEach(aTransformer()).transform(emptyList()), emptyIterable());
		assertThat(flatTransformEach(aTransformer()).transform(emptySet()), emptyIterable());
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(flatTransformEach(aTransformer()), isA(TransformerUtils.Transformer.class));
	}

	@Test
	void canUseFlatTransformEachFromList() {
		Transformer<Iterable<Integer>, List<? extends Integer>> transformer1 = flatTransformEach(ImmutableList::of);
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<Iterable<Integer>, List<Integer>> transformer2 = flatTransformEach(ImmutableList::of);
		assertThat(transformer2.transform(asList(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseFlatTransformEachFromIterable() {
		Transformer<Iterable<Integer>, Iterable<? extends Integer>> transformer1 = flatTransformEach(ImmutableList::of);
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<Iterable<Integer>, Iterable<Integer>> transformer2 = flatTransformEach(ImmutableList::of);
		assertThat(transformer2.transform(ImmutableSet.of(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseFlatTransformEachFromSet() {
		Transformer<Iterable<Integer>, Set<? extends Integer>> transformer1 = flatTransformEach(ImmutableList::of);
		assertThat(transformer1.transform(ImmutableSet.of(1, 2, 3)), iterableWithSize(3));

		Transformer<Iterable<Integer>, Set<Integer>> transformer2 = flatTransformEach(ImmutableList::of);
		assertThat(transformer2.transform(ImmutableSet.of(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void checkToString() {
		assertThat(flatTransformEach(aTransformer()),
			hasToString("TransformerUtils.flatTransformEach(aTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(flatTransformEach(aTransformer()), flatTransformEach(aTransformer()))
			.addEqualityGroup(flatTransformEach(ImmutableList::of))
			.testEquals();
	}

	private static Transformer<Iterable<String>, String> onlyIf(Predicate<? super String> predicate) {
		return s -> predicate.test(s) ? of(s) : of();
	}

	private static Transformer<Iterable<String>, String> prefixWith(String prefix) {
		return s -> of(prefix + s);
	}

	private static Transformer<Iterable<Integer>, String> length() {
		return s -> of(s.length());
	}
}
