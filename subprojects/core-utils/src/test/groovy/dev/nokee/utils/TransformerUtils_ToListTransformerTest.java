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
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.utils.TransformerUtils.Transformer;
import static dev.nokee.utils.TransformerUtils.constant;
import static dev.nokee.utils.TransformerUtils.toListTransformer;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isA;
import static org.hamcrest.Matchers.iterableWithSize;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransformerUtils_ToListTransformerTest {
	@Test
	void alwaysReturnTheSameElements() {
		assertThat(toListTransformer().transform(asList("a", "b", "c")), containsInAnyOrder("a", "b", "c"));
		assertThat(toListTransformer().transform(of("a", "b", "c")), containsInAnyOrder("a", "b", "c"));
	}

	@Test
	void canCastEachElementsToSpecifiedType() {
		Set<Object> obj = of("a", "b", "c");
		val result = assertDoesNotThrow(() -> toListTransformer(String.class).transform(obj));
		assertThat(result, containsInAnyOrder("a", "b", "c"));
	}

	@Test
	void throwsExceptionWhenCastingObject() {
		assertThrows(ClassCastException.class, () -> toListTransformer(Number.class).transform(of("a", "b", "c")));
	}

	@Test
	void canUseToListTransformerFromIterableWithTypeChecking() {
		Transformer<List<Integer>, Iterable<?>> transformer1 = toListTransformer(Integer.class);
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<List<Integer>, Iterable<Object>> transformer2 = toListTransformer(Integer.class);
		assertThat(transformer2.transform(asList(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseToListTransformerFromSetWithTypeChecking() {
		Transformer<List<Integer>, Set<?>> transformer1 = toListTransformer(Integer.class);
		assertThat(transformer1.transform(of(1, 2, 3)), iterableWithSize(3));

		Transformer<List<Integer>, Set<Object>> transformer2 = toListTransformer(Integer.class);
		assertThat(transformer2.transform(of(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseToListTransformerFromListWithTypeChecking() {
		Transformer<List<Integer>, List<?>> transformer1 = toListTransformer(Integer.class);
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<List<Integer>, List<Object>> transformer2 = toListTransformer(Integer.class);
		assertThat(transformer2.transform(asList(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseToListTransformerForSameElementTypes() {
		Transformer<List<String>, Iterable<String>> transformer1 = toListTransformer();
		Transformer<List<String>, List<String>> transformer2 = toListTransformer();
		Transformer<List<String>, Set<String>> transformer3 = toListTransformer();

		assertThat(transformer1.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer3.transform(of("1", "2")), iterableWithSize(2));
	}

	@Test
	void canUseToListTransformerForSuperElementTypes() {
		Transformer<List<Object>, Iterable<String>> transformer1 = toListTransformer();
		Transformer<List<Object>, List<String>> transformer2 = toListTransformer();
		Transformer<List<Object>, Set<String>> transformer3 = toListTransformer();

		assertThat(transformer1.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer3.transform(of("1", "2")), iterableWithSize(2));
	}

	@Test
	void checkToString() {
		assertThat(toListTransformer(), hasToString("TransformerUtils.toListTransformer()"));
		assertThat(toListTransformer(String.class), hasToString("TransformerUtils.toListTransformer(class java.lang.String)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(toListTransformer(), toListTransformer())
			.addEqualityGroup(toListTransformer(String.class), toListTransformer(String.class))
			.addEqualityGroup(toListTransformer(Number.class))
			.testEquals();
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(toListTransformer(), isA(TransformerUtils.Transformer.class));
		assertThat(toListTransformer(String.class), isA(TransformerUtils.Transformer.class));
	}

	@Test
	void canChainIterableReturningTransformerToListTransformer() {
		assertThat(constant(asList("a", "b", "c")).andThen(toListTransformer()).transform(new Object()),
			allOf(isA(List.class), contains("a", "b", "c")));
	}

	@Test
	void returnsNoCastToListTransformerForObjectType() {
		assertThat(toListTransformer(Object.class), equalTo(toListTransformer()));
	}

	@Test
	void canSerialize() {
		assertThat(toListTransformer(), isSerializable());
		assertThat(toListTransformer(String.class), isSerializable());
	}
}
