package dev.nokee.model.internal.registry;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UnmanagedCreatingModelProjectionTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private final ModelProjection subject = UnmanagedCreatingModelProjection.of(TYPE, MyType::new);

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

	@Test
	void delegateToFactoryWhenProjectionIsResolved() {
		@SuppressWarnings("unchecked")
		val factory = (Factory<MyType>) Mockito.mock(Factory.class);
		val projection = UnmanagedCreatingModelProjection.of(TYPE, factory);

		// Each calls delegate once to factory
		projection.get(TYPE);
		projection.get(TYPE);
		projection.get(TYPE);

		verify(factory, times(3)).create();
	}

	interface BaseType {}
	static class MyType implements BaseType {}
	interface WrongType {}
}
