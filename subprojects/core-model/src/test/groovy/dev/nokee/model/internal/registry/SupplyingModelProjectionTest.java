package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.type.ModelType;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.internal.Cast;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Supplier;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.mockito.Mockito.*;

@Subject(SupplyingModelProjection.class)
class SupplyingModelProjectionTest extends TypeCompatibilityModelProjectionSupportTest {
	private static final ModelType<MyType> TYPE = ModelType.of(MyType.class);
	private static final MyType INSTANCE = new MyType();

	private static SupplyingModelProjection<MyType> createSubject(Supplier<MyType> supplier) {
		return new SupplyingModelProjection<>(TYPE, supplier);
	}

	private static <T> SupplyingModelProjection<T> createSubject(Class<T> type, Supplier<T> supplier) {
		return new SupplyingModelProjection<>(of(type), supplier);
	}

	@Test
	void delegateToFactoryWhenProjectionIsResolved() {
		Supplier<MyType> supplier = Cast.uncheckedCast(mock(Supplier.class));
		val projection = createSubject(supplier);

		// Each calls delegate once to factory
		projection.get(TYPE);
		projection.get(TYPE);
		projection.get(TYPE);

		verify(supplier, times(3)).get();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().setDefault(ModelType.class, TYPE).testAllPublicStaticMethods(SupplyingModelProjection.class);
	}

	@Test
	void checkToString() {
		assertThat(createSubject(ofInstance(new MyType())),
			hasToString("SupplyingModelProjection.of(class dev.nokee.model.internal.registry.SupplyingModelProjectionTest$MyType, Suppliers.ofInstance(MyType))"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(ofInstance(INSTANCE)), createSubject(ofInstance(INSTANCE)))
			.addEqualityGroup(createSubject(() -> null))
			.addEqualityGroup(createSubject(BaseType.class, () -> null))
			.testEquals();
	}

	@SneakyThrows
	@Override
	protected ModelProjection createSubject(Class<?> type) {
		return createSubject((Class<Object>)type, () -> null);
	}

	interface BaseType {}
	static class MyType implements BaseType {
		@Override
		public String toString() {
			return "MyType";
		}
	}
}
