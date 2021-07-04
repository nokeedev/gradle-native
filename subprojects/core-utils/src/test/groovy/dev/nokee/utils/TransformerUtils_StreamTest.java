package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.function.Function;

import static dev.nokee.utils.TransformerUtils.stream;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class TransformerUtils_StreamTest {
	@Test
	void canManipulateStream() {
		assertThat(stream(it -> it.map(Object::toString).collect(joining("-"))).transform(Arrays.asList("foo", "bar")), equalTo("foo-bar"));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(stream(it -> it.map(Object::toString).collect(toList())), isA(TransformerUtils.Transformer.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(stream(new TestFunction("a")), stream(new TestFunction("a")))
			.addEqualityGroup(stream(new TestFunction("b")))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(stream(new TestFunction("a")), hasToString(startsWith("TransformerUtils.stream(TestFunction(a))")));
	}

	@EqualsAndHashCode
	private static final class TestFunction implements Function<Object, Object> {
		private final String what;

		private TestFunction(String what) {
			this.what = what;
		}

		@Override
		public Object apply(Object o) {
			throw new UnsupportedOperationException();
		}

		@Override
		public String toString() {
			return "TestFunction(" + what + ")";
		}
	}
}
