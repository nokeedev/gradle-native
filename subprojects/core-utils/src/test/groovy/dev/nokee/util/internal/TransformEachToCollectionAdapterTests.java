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

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.util.internal.GuavaImmutableCollectionBuilderFactories.listFactory;
import static dev.nokee.util.internal.GuavaImmutableCollectionBuilderFactories.setFactory;
import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasToString;

class TransformEachToCollectionAdapterTests {
	@Nested
	class WhenElementsMapOneToOne {
		TransformEachToCollectionAdapter<List<String>, String, String> subject = new TransformEachToCollectionAdapter<>(listFactory(), prefixWith("a-"));
		List<String> result = subject.transform(of("bar", "foo", "far"));

		@Test
		void checkTransformation() {
			assertThat(result, contains("a-bar", "a-foo", "a-far"));
		}
	}

	@Nested
	class WhenElementsMapToAnotherType {
		TransformEachToCollectionAdapter<List<Integer>, Integer, String> subject = new TransformEachToCollectionAdapter<>(listFactory(), String::length);
		List<Integer> result = subject.transform(of("bar", "foobar", "foobarfar"));

		@Test
		void checkTransformation() {
			assertThat(result, contains(3, 6, 9));
		}
	}

	@Nested
	class WhenNoElementsToMap {
		TransformEachToCollectionAdapter<List<String>, String, String> subject = new TransformEachToCollectionAdapter<>(listFactory(), alwaysThrows());
		List<String> result = subject.transform(of());

		@Test
		void checkTransformation() {
			assertThat(result, emptyIterable());
		}
	}

	@Test
	void checkToString() {
		assertThat(new TransformEachToCollectionAdapter<>(listFactory(), aTransformer()),
			hasToString("TransformerUtils.transformEach(aTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(new TransformEachToCollectionAdapter<>(listFactory(), aTransformer()), new TransformEachToCollectionAdapter<>(listFactory(), aTransformer()))
			.addEqualityGroup(new TransformEachToCollectionAdapter<>(setFactory(), aTransformer()))
			.addEqualityGroup(new TransformEachToCollectionAdapter<>(setFactory(), anotherTransformer()))
			.testEquals();
	}

	@Test
	void canSerialize() {
		assertThat(new TransformEachToCollectionAdapter<>(listFactory(), aTransformer()), isSerializable());
	}

	private static Transformer<String, String> prefixWith(String prefix) {
		return s -> prefix + s;
	}

	private static <OUT, IN> Transformer<OUT, IN> alwaysThrows() {
		return __ -> { throw new UnsupportedOperationException(); };
	}
}
