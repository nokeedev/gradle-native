package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class ModelType_ParameterizedTypeTest {
	@Test
	void nonParameterizedType() {
		assertThat(of(MyType.class).isParameterized(), equalTo(false));
	}

	@Test
	void nonParameterizedTypeHasNoTypeVariables() {
		assertThat(of(MyType.class).getTypeVariables(), empty());
	}

	@Test
	void parameterizedType() {
		assertThat(of(new TypeOf<MyGenericType<String>>() {}).isParameterized(), equalTo(true));
	}

	@Test
	void parameterizedTypeHasTypeVariables() {
		assertThat(of(new TypeOf<MyGenericType<String>>() {}).getTypeVariables(), contains(of(String.class)));
	}

	interface MyType {}
	interface MyGenericType<T> {}
}
