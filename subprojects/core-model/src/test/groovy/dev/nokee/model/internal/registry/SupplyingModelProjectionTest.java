package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.Factory;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import spock.lang.Subject;

import static dev.nokee.internal.Factories.alwaysThrow;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Subject(SupplyingModelProjection.class)
class SupplyingModelProjectionTest extends TypeCompatibilityModelProjectionSupportTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);

	private static SupplyingModelProjection<MyType> createSubject(Factory<MyType> factory) {
		return new SupplyingModelProjection<>(TYPE, factory);
	}

	private static <T> SupplyingModelProjection<T> createSubject(Class<T> type, Factory<T> factory) {
		return new SupplyingModelProjection<>(of(type), factory);
	}

	@Test
	void delegateToFactoryWhenProjectionIsResolved() {
		@SuppressWarnings("unchecked")
		val factory = (Factory<MyType>) Mockito.mock(Factory.class);
		val projection = createSubject(factory);

		// Each calls delegate once to factory
		projection.get(TYPE);
		projection.get(TYPE);
		projection.get(TYPE);

		verify(factory, times(3)).create();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelType.class, TYPE).testAllPublicStaticMethods(SupplyingModelProjection.class);
	}

	@Test
	void checkToString() {
		assertThat(createSubject(alwaysThrow()),
			hasToString("SupplyingModelProjection.of(class dev.nokee.model.internal.registry.SupplyingModelProjectionTest$MyType, Factories.alwaysThrow())"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(alwaysThrow()), createSubject(alwaysThrow()))
			.addEqualityGroup(createSubject(() -> null))
			.addEqualityGroup(createSubject(BaseType.class, alwaysThrow()))
			.testEquals();
	}

	@Override
	protected ModelProjection createSubject(Class<?> type) {
		return createSubject(alwaysThrow());
	}

	interface BaseType {}
	static class MyType implements BaseType {}
}
