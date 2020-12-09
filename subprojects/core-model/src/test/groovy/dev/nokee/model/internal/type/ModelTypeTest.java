package dev.nokee.model.internal.type;

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTypeTest {
	@Test
	void canAccessRawType() {
		assertAll(() -> {
			assertEquals(String.class, of(String.class).getRawType());
			assertEquals(Integer.class, of(Integer.class).getRawType());
			assertEquals(MyType.class, of(MyType.class).getRawType());
		});
	}

	@Test
	void canAccessConcreteType() {
		assertAll(() -> {
			assertEquals(String.class, of(String.class).getConcreteType());
			assertEquals(Integer.class, of(Integer.class).getConcreteType());
			assertEquals(MyType.class, of(MyType.class).getConcreteType());
		});
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ModelType.class);
	}

	@Test
	void canCheckAssignableCompatibility() {
		val myType = of(MyType.class);
		assertAll(() -> {
			assertTrue(myType.isAssignableFrom(myType), "same instance should be assignable");
			assertTrue(of(MyType.class).isAssignableFrom(of(MyType.class)), "same type should be assignable");
			assertTrue(of(Object.class).isAssignableFrom(of(MyType.class)), "all types are assignable to Object");
			assertFalse(of(String.class).isAssignableFrom(of(MyType.class)), "unrelated types should not be assignable");
		});
	}

	interface MyType {}
}
