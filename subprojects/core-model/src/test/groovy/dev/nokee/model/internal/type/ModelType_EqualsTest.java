package dev.nokee.model.internal.type;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;

class ModelType_EqualsTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canEquals() {
		new EqualsTester()
			.addEqualityGroup(of(String.class), of(String.class))
			.addEqualityGroup(of(Integer.class))
			.addEqualityGroup(of(new TypeOf<MyList<String>>() {}), of(MyStringList.class).getSupertype().get())
			.testEquals();
	}

	static class MyList<T> {}
	static class MyStringList extends MyList<String> {}
}
