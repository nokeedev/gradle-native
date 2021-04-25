package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.utils.TransformerTestUtils.aTransformer;
import static dev.nokee.utils.TransformerTestUtils.anotherTransformer;
import static dev.nokee.utils.TransformerUtils.compose;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Subject(TransformerUtils.class)
class TransformerUtils_ComposeTest {
	@Test
	void canComposeTransformers() {
		assertThat(compose(prefixWith("second-"), prefixWith("first-")).transform("foo"),
			equalTo("second-first-foo"));
	}

	@Test
	void returnsEnhanceTransformer() {
		assertThat(compose(aTransformer(), aTransformer()), isA(TransformerUtils.Transformer.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(compose(aTransformer(), aTransformer()), compose(aTransformer(), aTransformer()))
			.addEqualityGroup(compose(anotherTransformer(), aTransformer()))
			.addEqualityGroup(compose(aTransformer(), anotherTransformer()))
			.addEqualityGroup(compose(noOpTransformer(), noOpTransformer()), noOpTransformer())
			.addEqualityGroup(compose(aTransformer(), noOpTransformer()), aTransformer())
			.addEqualityGroup(compose(noOpTransformer(), anotherTransformer()), anotherTransformer())
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(compose(aTransformer(), aTransformer()),
			hasToString("TransformerUtils.compose(aTransformer(), aTransformer())"));
	}

	@Test
	void canComposeFromTransformer() {
		assertThat(prefixWith("after-").compose(prefixWith("before-")).transform("bar"),
			equalTo("after-before-bar"));
	}

	@Test
	void canAndThen() {
		assertThat(prefixWith("-").andThen(prefixWith("then")).transform("far"),
			equalTo("then-far"));
	}

	private static TransformerUtils.Transformer<String, String> prefixWith(String prefix) {
		return s -> prefix + s;
	}
}
