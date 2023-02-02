/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.util.internal;

import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newMock;
import static dev.nokee.internal.testing.testdoubles.TestDouble.narrowed;
import static dev.nokee.util.internal.GuavaImmutableCollectionBuilderFactories.listFactory;
import static dev.nokee.util.internal.GuavaImmutableCollectionBuilderFactories.setFactory;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasToString;

class FlatTransformEachToCollectionAdapterTests {
	@Nested
	class WhenElementsMapOneToOne {
		FlatTransformEachToCollectionAdapter<List<String>, String, String> subject = new FlatTransformEachToCollectionAdapter<>(listFactory(), prefixWith("a"));
		List<String> result = subject.transform(of("foo", "bar", "far"));

		@Test
		void checkTransformation() {
			assertThat(result, contains("a-foo", "a-bar", "a-far"));
		}
	}

	@Nested
	class WhenElementsMapOneToMultiple {
		FlatTransformEachToCollectionAdapter<List<String>, String, String> subject = new FlatTransformEachToCollectionAdapter<>(listFactory(), prefixWith("a", "b"));
		List<String> result = subject.transform(of("foo", "bar", "far"));

		@Test
		void checkTransformation() {
			assertThat(result, contains("a-foo", "b-foo", "a-bar", "b-bar", "a-far", "b-far"));
		}
	}

	@Nested
	class WhenElementsMapOneToNone {
		FlatTransformEachToCollectionAdapter<List<String>, String, String> subject = new FlatTransformEachToCollectionAdapter<>(listFactory(), onlyIf("foo"::equals));
		List<String> result = subject.transform(of("foo", "bar", "far"));

		@Test
		void checkTransformation() {
			assertThat(result, contains("foo"));
		}
	}


	@Nested
	class WhenNoElementsToMap {
		FlatTransformEachToCollectionAdapter<List<String>, String, String> subject = new FlatTransformEachToCollectionAdapter<>(listFactory(), alwaysThrows());
		List<String> result = subject.transform(of());

		@Test
		void checkTransformation() {
			assertThat(result, emptyIterable());
		}
	}

	@Nested
	class WhenElementsMapToAnotherType {
		FlatTransformEachToCollectionAdapter<List<Integer>, Integer, String> subject = new FlatTransformEachToCollectionAdapter<>(listFactory(), length());
		List<Integer> result = subject.transform(of("bar", "foobar", "foobarfar"));

		@Test
		void checkTransformation() {
			assertThat(result, contains(3, 6, 9));
		}
	}

	@Test
	void checkToString() {
		assertThat(new FlatTransformEachToCollectionAdapter<>(listFactory(), aTransformer()),
			hasToString("TransformerUtils.flatTransformEach(aTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(new FlatTransformEachToCollectionAdapter<>(listFactory(), aTransformer()),
				new FlatTransformEachToCollectionAdapter<>(listFactory(), aTransformer()))
			.addEqualityGroup(new FlatTransformEachToCollectionAdapter<>(setFactory(), aTransformer()))
			.addEqualityGroup(new FlatTransformEachToCollectionAdapter<>(setFactory(), anotherTransformer()))
			.testEquals();
	}

	@Test
	void canSerialize() {
		assertThat(new FlatTransformEachToCollectionAdapter<>(listFactory(), aTransformer()), isSerializable());
	}

	private static Transformer<Iterable<String>, String> onlyIf(Predicate<? super String> predicate) {
		return s -> predicate.test(s) ? of(s) : of();
	}

	private static Transformer<List<String>, String> prefixWith(String... prefixes) {
		return s -> Stream.of(prefixes).map(it -> it + "-" + s).collect(Collectors.toList());
	}

	private static Transformer<Iterable<Integer>, String> length() {
		return s -> of(s.length());
	}

	private static <OUT, IN> Transformer<Iterable<OUT>, IN> alwaysThrows() {
		return newMock(Transformer.class).alwaysThrows().instance(narrowed());
	}
}
