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
		Transformer<List<String>, Iterable<? extends String>> transformer1 = toListTransformer();
		Transformer<List<String>, List<? extends String>> transformer2 = toListTransformer();
		Transformer<List<String>, Set<? extends String>> transformer3 = toListTransformer();
		Transformer<List<String>, Iterable<String>> transformer4 = toListTransformer();
		Transformer<List<String>, List<String>> transformer5 = toListTransformer();
		Transformer<List<String>, Set<String>> transformer6 = toListTransformer();

		assertThat(transformer1.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer3.transform(of("1", "2")), iterableWithSize(2));
		assertThat(transformer4.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer5.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer6.transform(of("1", "2")), iterableWithSize(2));
	}

	@Test
	void canUseToListTransformerForSuperElementTypes() {
		Transformer<List<Object>, Iterable<? extends String>> transformer1 = toListTransformer();
		Transformer<List<Object>, List<? extends String>> transformer2 = toListTransformer();
		Transformer<List<Object>, Set<? extends String>> transformer3 = toListTransformer();
		Transformer<List<Object>, Iterable<String>> transformer4 = toListTransformer();
		Transformer<List<Object>, List<String>> transformer5 = toListTransformer();
		Transformer<List<Object>, Set<String>> transformer6 = toListTransformer();

		assertThat(transformer1.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer3.transform(of("1", "2")), iterableWithSize(2));
		assertThat(transformer4.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer5.transform(asList("1", "2")), iterableWithSize(2));
		assertThat(transformer6.transform(of("1", "2")), iterableWithSize(2));
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
}
