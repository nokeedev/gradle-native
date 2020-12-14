package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.Factory;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class UnmanagedCreatingModelProjectionTest extends TypeCompatibilityModelProjectionSupportTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);

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

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelType.class, TYPE).testAllPublicStaticMethods(UnmanagedCreatingModelProjection.class);
	}

	@Test
	void checkToString() {
		assertThat(UnmanagedCreatingModelProjection.of(TYPE, alwaysThrow()),
			hasToString("UnmanagedCreatingModelProjection.of(class dev.nokee.model.internal.registry.UnmanagedCreatingModelProjectionTest$MyType, Factories.alwaysThrow())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(UnmanagedCreatingModelProjection.of(TYPE, alwaysThrow()), UnmanagedCreatingModelProjection.of(TYPE, alwaysThrow()))
			.addEqualityGroup(UnmanagedCreatingModelProjection.of(TYPE, () -> null))
			.addEqualityGroup(UnmanagedCreatingModelProjection.of(ModelType.of(BaseType.class), alwaysThrow()))
			.testEquals();
	}

	@Override
	protected ModelProjection createSubject(Class<?> type) {
		return UnmanagedCreatingModelProjection.of(of(type), alwaysThrow());
	}

	interface BaseType {}
	static class MyType implements BaseType {}
}
