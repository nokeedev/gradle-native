package dev.gradleplugins.grava.util;

import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.gradle.api.specs.Specs;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static com.google.common.collect.ImmutableSet.of;
import static dev.gradleplugins.grava.testing.util.SpecTestUtils.aSpec;
import static dev.gradleplugins.grava.testing.util.SpecTestUtils.anotherSpec;
import static dev.gradleplugins.grava.util.TransformerUtils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TransformerUtils_MatchingTest {
	@Test
	void canUseMatching() {
		Transformer<Iterable<String>, Iterable<? extends String>> transformer1 = matching(it -> true);
		Transformer<Iterable<String>, List<? extends String>> transformer2 = matching(it -> true);
		Transformer<Iterable<String>, Set<? extends String>> transformer3 = matching(it -> true);
		Transformer<Iterable<String>, Iterable<String>> transformer4 = matching(it -> true);
		Transformer<Iterable<String>, List<String>> transformer5 = matching(it -> true);
		Transformer<Iterable<String>, Set<String>> transformer6 = matching(it -> true);

		assertThat(transformer1.transform(asList("a", "b")), iterableWithSize(2));
		assertThat(transformer2.transform(asList("a", "b")), iterableWithSize(2));
		assertThat(transformer3.transform(of("a", "b")), iterableWithSize(2));
		assertThat(transformer4.transform(asList("a", "b")), iterableWithSize(2));
		assertThat(transformer5.transform(asList("a", "b")), iterableWithSize(2));
		assertThat(transformer6.transform(of("a", "b")), iterableWithSize(2));
	}

	@Test
	void returnOnlyElementsMatchingSpec() {
		assertThat(matching(it -> !it.equals("foo")).transform(asList("bar", "foo", "far")),
			contains("bar", "far"));
	}

	@Test
	void canFilterSet() {
		assertThat(matching(it -> !it.equals("foo")).transform(of("bar", "foo", "far")),
			containsInAnyOrder("bar", "far"));
	}

	@Test
	void returnsEmptyListForEmptyInputSet() {
		assertThat(matching(it -> true).transform(emptyList()), emptyIterable());
	}

	@Test
	void checkToString() {
		assertThat(matching(aSpec()), hasToString("TransformerUtils.matching(aSpec())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(matching(aSpec()), matching(aSpec()))
			.addEqualityGroup(matching(anotherSpec()))
			.testEquals();
	}

	@Test
	void returnsNoOpTransformerForObviousSatisfyAllSpec() {
		assertThat(matching(Specs.satisfyAll()), equalTo(noOpTransformer()));
		assertThat(matching(dev.gradleplugins.grava.util.SpecUtils.satisfyAll()), equalTo(noOpTransformer()));
	}

	@Test
	void returnsConstantOfEmptyListForObviousSatisfyNoneSpec() {
		assertThat(matching(Specs.satisfyNone()), equalTo(constant(emptyList())));
		assertThat(matching(SpecUtils.satisfyNone()), equalTo(constant(emptyList())));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(matching(aSpec()), isA(TransformerUtils.Transformer.class));
	}
}
