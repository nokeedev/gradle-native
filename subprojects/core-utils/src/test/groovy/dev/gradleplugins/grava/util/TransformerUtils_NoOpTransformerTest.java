package dev.gradleplugins.grava.util;

import dev.gradleplugins.grava.util.TransformerUtils;
import lombok.val;
import org.gradle.api.Transformer;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.gradleplugins.grava.util.TransformerUtils.noOpTransformer;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

@Subject(TransformerUtils.class)
class TransformerUtils_NoOpTransformerTest {
	@Test
	void alwaysReturnTheInputObject() {
		val obj1 = new Object();
		val obj2 = "obj";
		val obj3 = new Double(4.2);

		assertThat(noOpTransformer().transform(obj1), equalTo(obj1));
		assertThat(noOpTransformer().transform(obj2), equalTo(obj2));
		assertThat(noOpTransformer().transform(obj3), equalTo(obj3));
	}

	@Test
	void alwaysReturnTheSameInstance() {
		assertThat(noOpTransformer(), equalTo(noOpTransformer()));
	}

	@Test
	void checkToString() {
		assertThat(noOpTransformer(), hasToString("TransformerUtils.noOpTransformer()"));
	}

	@Test
	void canUseNoOpTransformerWhenOutputTypeIsASuperTypeOfTheInputType() {
		// compile-time assertion
		inputTypeToSuperType(noOpTransformer());
	}

	@Test
	void cannotUseNoOpTransformerWhenOutputTypeIsAChildTypeOfTheInputType() {
		// compile-time assertion, uncomment to test -> it should not compile
//		inputTypeToChildType(noOpTransformer());
	}

	@Test
	void canUseNoOpTransformerWhenOutputTypeIsTheSameAsInputType() {
		// compile-time assertion, uncomment to test -> it should not compile
		bothInputAndOutputTypeAreTheSame(noOpTransformer());
	}

	private static void inputTypeToSuperType(Transformer<Base, Child> transformer) {}
	private static void inputTypeToChildType(Transformer<Child, Base> transformer) {}
	private static void bothInputAndOutputTypeAreTheSame(Transformer<Base, Base> transformer) {}

	interface Base {}
	interface Child extends Base {}
}
