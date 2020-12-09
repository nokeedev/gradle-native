package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class ModelType_ToStringTest {
	@Test
	void checkToStringForInterfaceType() {
		assertThat(of(MyInterfaceType.class), hasToString("interface dev.nokee.model.internal.type.ModelType_ToStringTest$MyInterfaceType"));
	}

	@Test
	void checkToStringForClassType() {
		assertThat(of(MyClassType.class), hasToString("class dev.nokee.model.internal.type.ModelType_ToStringTest$MyClassType"));
	}

	@Test
	void checkToStringForGenericInterfaceType() {
		assertThat(of(new TypeOf<GenericList<String>>() {}),
			hasToString("interface dev.nokee.model.internal.type.ModelType_ToStringTest$GenericList<java.lang.String>"));
	}

	@Test
	void checkToStringForGenericClassType() {
		assertThat(of(new TypeOf<BaseList<Integer>>() {}),
			hasToString("class dev.nokee.model.internal.type.ModelType_ToStringTest$BaseList<java.lang.Integer>"));
	}

	interface MyInterfaceType {}
	static class MyClassType {}
	interface GenericList<T> {}
	static class BaseList<T> {}

}
