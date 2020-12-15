package dev.nokee.internal;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.UnaryOperator;

import static com.google.common.base.Functions.identity;
import static dev.nokee.internal.Factories.compose;
import static dev.nokee.internal.Factories.constant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(Factories.class)
class Factories_ComposeTest {
	@Test
	void callsComposingFunctionWithFactoryReturnValue() {
		UnaryOperator<MyType> function = Cast.uncheckedCast(mock(UnaryOperator.class));
		val instance = new MyType();
		compose(constant(instance), function).create();
		verify(function, times(1)).apply(instance);
	}

	@Test
	void returnsTransformedValue() {
		assertThat(compose(MyType::new, MyType::toString).create(), equalTo("MyType#toString()"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(compose(constant(42), identity()), compose(constant(42), identity()))
			.addEqualityGroup(compose(constant(24), identity()))
			.addEqualityGroup(compose(constant(42), t -> t))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(compose(constant(42), identity()), hasToString("Factories.compose(Factories.constant(42), Functions.identity())"));
	}

	static class MyType {
		@Override
		public String toString() {
			return "MyType#toString()";
		}
	}
}
