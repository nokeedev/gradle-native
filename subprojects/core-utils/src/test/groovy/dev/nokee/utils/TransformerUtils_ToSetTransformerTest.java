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
import static dev.nokee.utils.TransformerUtils.*;
import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TransformerUtils_ToSetTransformerTest {
	@Test
	void alwaysReturnTheSameElements() {
		assertThat(toSetTransformer().transform(asList("a", "b", "c")), containsInAnyOrder("a", "b", "c"));
		assertThat(toSetTransformer().transform(of("a", "b", "c")), containsInAnyOrder("a", "b", "c"));
	}

	@Test
	void canCastEachElementsToSpecifiedType() {
		Set<Object> obj = of("a", "b", "c");
		val result = assertDoesNotThrow(() -> toSetTransformer(String.class).transform(obj));
		assertThat(result, containsInAnyOrder("a", "b", "c"));
	}

	@Test
	void throwsExceptionWhenCastingObject() {
		assertThrows(ClassCastException.class, () -> toSetTransformer(Number.class).transform(of("a", "b", "c")));
	}

	@Test
	void canUseToSetTransformerFromIterableWithTypeChecking() {
		Transformer<Set<Integer>, Iterable<?>> transformer1 = toSetTransformer(Integer.class);
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<Set<Integer>, Iterable<Object>> transformer2 = toSetTransformer(Integer.class);
		assertThat(transformer2.transform(asList(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseToSetTransformerFromSetWithTypeChecking() {
		Transformer<Set<Integer>, Set<?>> transformer1 = toSetTransformer(Integer.class);
		assertThat(transformer1.transform(of(1, 2, 3)), iterableWithSize(3));

		Transformer<Set<Integer>, Set<Object>> transformer2 = toSetTransformer(Integer.class);
		assertThat(transformer2.transform(of(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseToSetTransformerFromListWithTypeChecking() {
		Transformer<Set<Integer>, List<?>> transformer1 = toSetTransformer(Integer.class);
		assertThat(transformer1.transform(asList(1, 2, 3)), iterableWithSize(3));

		Transformer<Set<Integer>, List<Object>> transformer2 = toSetTransformer(Integer.class);
		assertThat(transformer2.transform(asList(1, 2, 3)), iterableWithSize(3));
	}

	@Test
	void canUseToSetTransformerForSameElementTypes() {
		Transformer<Set<String>, Iterable<? extends String>> transformer1 = toSetTransformer();
		Transformer<Set<String>, List<? extends String>> transformer2 = toSetTransformer();
		Transformer<Set<String>, Set<? extends String>> transformer3 = toSetTransformer();
		Transformer<Set<String>, Iterable<String>> transformer4 = toSetTransformer();
		Transformer<Set<String>, List<String>> transformer5 = toSetTransformer();
		Transformer<Set<String>, Set<String>> transformer6 = toSetTransformer();

		assertThat(transformer1.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer3.transform(of("1", "2")), iterableWithSize(2));
		assertThat(transformer4.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer5.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer6.transform(of("1", "2")), iterableWithSize(2));
	}

	@Test
	void canUseToSetTransformerForSuperElementTypes() {
		Transformer<Set<Object>, Iterable<? extends String>> transformer1 = toSetTransformer();
		Transformer<Set<Object>, List<? extends String>> transformer2 = toSetTransformer();
		Transformer<Set<Object>, Set<? extends String>> transformer3 = toSetTransformer();
		Transformer<Set<Object>, Iterable<String>> transformer4 = toSetTransformer();
		Transformer<Set<Object>, List<String>> transformer5 = toSetTransformer();
		Transformer<Set<Object>, Set<String>> transformer6 = toSetTransformer();

		assertThat(transformer1.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer3.transform(of("1", "2")), iterableWithSize(2));
		assertThat(transformer4.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer5.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer6.transform(of("1", "2")), iterableWithSize(2));
	}

	@Test
	void checkToString() {
		assertThat(toSetTransformer(), hasToString("TransformerUtils.toSetTransformer()"));
		assertThat(toSetTransformer(String.class), hasToString("TransformerUtils.toSetTransformer(class java.lang.String)"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(toSetTransformer(), toSetTransformer())
			.addEqualityGroup(toSetTransformer(String.class), toSetTransformer(String.class))
			.addEqualityGroup(toSetTransformer(Number.class))
			.testEquals();
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(toSetTransformer(), isA(TransformerUtils.Transformer.class));
		assertThat(toSetTransformer(String.class), isA(TransformerUtils.Transformer.class));
	}

	@Test
	void canChainIterableReturningTransformerToSetTransformer() {
		assertThat(constant(asList("a", "b", "c")).andThen(toSetTransformer()).transform(new Object()),
			allOf(isA(Set.class), containsInAnyOrder("a", "b", "c")));
	}

	@Test
	void returnsNoCastToSetTransformerForObjectType() {
		assertThat(toSetTransformer(Object.class), equalTo(toSetTransformer()));
	}
}
