package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.utils.TransformerUtils.compose;
import static dev.nokee.utils.TransformerUtils.noOpTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

@Subject(TransformerUtils.class)
class TransformerUtils_ComposeTest {
	@Test
	void canComposeTransformers() {
		assertThat(compose(prefixWith("second-"), prefixWith("first-")).transform("foo"),
			equalTo("second-first-foo"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(compose(noOpTransformer(), noOpTransformer()), compose(noOpTransformer(), noOpTransformer()))
			.addEqualityGroup(compose(t -> t, noOpTransformer()))
			.addEqualityGroup(compose(noOpTransformer(), t -> t))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(compose(noOpTransformer(), noOpTransformer()),
			hasToString("TransformerUtils.compose(TransformerUtils.noOpTransformer(), TransformerUtils.noOpTransformer())"));
	}

	private static TransformerUtils.Transformer<String, String> prefixWith(String prefix) {
		return s -> prefix + s;
	}
}
