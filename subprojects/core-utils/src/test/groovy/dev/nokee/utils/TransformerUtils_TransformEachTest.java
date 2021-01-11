package dev.nokee.utils;

import com.google.common.collect.ImmutableSet;
import com.google.common.testing.EqualsTester;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static com.google.common.collect.ImmutableList.of;
import static dev.nokee.utils.TransformerUtils.*;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptySet;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Subject(TransformerUtils.class)
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
		assertThat(transformEach(noOpTransformer()).transform(emptyList()),	empty());
		assertThat(transformEach(noOpTransformer()).transform(emptySet()),	empty());
	}

	@Test
	void checkToString() {
		assertThat(transformEach(noOpTransformer()),
			hasToString("TransformerUtils.transformEach(TransformerUtils.noOpTransformer())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(transformEach(noOpTransformer()), transformEach(noOpTransformer()))
			.addEqualityGroup(transformEach(t -> t))
			.testEquals();
	}

	private static Transformer<String, String> prefixWith(String prefix) {
		return s -> prefix + s;
	}
}
