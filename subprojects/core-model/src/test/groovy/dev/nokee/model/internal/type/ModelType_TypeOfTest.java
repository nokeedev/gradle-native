package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.typeOf;
import static org.junit.jupiter.api.Assertions.assertEquals;

@Subject(ModelType.class)
class ModelType_TypeOfTest {
	@Test
	void canCreateModelTypeOfInstance() {
		assertEquals(of(MyType.class), typeOf(new MyType()), "type of MyType instance is expected to be MyType");
	}

	static final class MyType {}
}
