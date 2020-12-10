package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Subject(UnmanagedInstanceModelProjection.class)
class UnmanagedInstanceModelProjectionTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private final ModelProjection subject = UnmanagedInstanceModelProjection.of(new MyType());

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(UnmanagedInstanceModelProjection.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		val instance = new Object();
		new EqualsTester()
			.addEqualityGroup(UnmanagedInstanceModelProjection.of(instance), UnmanagedInstanceModelProjection.of(instance))
			.addEqualityGroup(UnmanagedInstanceModelProjection.of(new Object()))
			.testEquals();
	}

	@Test
	void canQueryExactProjectionType() {
		assertTrue(subject.canBeViewedAs(TYPE), "projection should be viewable as exact type");
	}

	@Test
	void canQueryBaseProjectionType() {
		assertTrue(subject.canBeViewedAs(of(BaseType.class)), "projection should be viewable as base type");
		assertTrue(subject.canBeViewedAs(of(Object.class)), "projection should be viewable as base type");
	}

	@Test
	void cannotQueryWrongProjectionType() {
		assertFalse(subject.canBeViewedAs(of(WrongType.class)), "projection should not be viewable for wrong type");
	}

	interface BaseType {}
	static class MyType implements BaseType {}
	interface WrongType {}
}
