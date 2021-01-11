package dev.nokee.utils;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Predicate;

import static com.google.common.base.Predicates.not;
import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.TransformerUtils.*;
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
		assertThat(flatTransformEach(noOpTransformer()).transform(emptyList()),	empty());
		assertThat(flatTransformEach(noOpTransformer()).transform(emptySet()),	empty());
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(flatTransformEach(noOpTransformer()), isA(TransformerUtils.Transformer.class));
	}

	@Test
	void checkToString() {
		assertThat(flatTransformEach(noOpTransformer()),
			hasToString("TransformerUtils.flatTransformEach(TransformerUtils.noOpTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(flatTransformEach(noOpTransformer()), flatTransformEach(noOpTransformer()))
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
