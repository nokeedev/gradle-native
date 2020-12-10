package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import spock.lang.Subject;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

@Subject(ManagedModelProjection.class)
class ManagedModelProjectionTest {
	@ParameterizedTest
	@EnumSource(Factory.class)
	void managedProjectionCannotBeUsedAsIs(Factory factory) {
		assertAll(() -> {
			val projection = factory.create(MyType.class);
			assertThrows(UnsupportedOperationException.class, () -> projection.canBeViewedAs(ModelType.of(MyType.class)));
			assertThrows(UnsupportedOperationException.class, () -> projection.get(ModelType.of(MyType.class)));
		});
	}

	@ParameterizedTest
	@EnumSource(Factory.class)
	void canBindProjectionToAnInstantiator(Factory factory) {
		assertAll(() -> {
			val projection = factory.create(MyType.class).bind(TestUtils.objectFactory());
			assertTrue(projection.canBeViewedAs(ModelType.of(MyType.class)));
			assertFalse(projection.canBeViewedAs(ModelType.of(WrongType.class)));

			assertThat(projection.get(ModelType.of(MyType.class)), isA(MyType.class));
		});
	}

	enum Factory {
		FactoryUsingRawType {
			<T> ManagedModelProjection<T> create(Class<T> type) {
				return ManagedModelProjection.of(type);
			}
		},
		FactoryUsingType {
			<T> ManagedModelProjection<T> create(Class<T> type) {
				return ManagedModelProjection.of(ModelType.of(type));
			}
		};

		abstract <T> ManagedModelProjection<T> create(Class<T> type);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ManagedModelProjection.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ManagedModelProjection.of(MyType.class), ManagedModelProjection.of(ModelType.of(MyType.class)))
			.addEqualityGroup(ManagedModelProjection.of(MyOtherType.class))
			.testEquals();
	}

	interface MyType {}
	interface MyOtherType {}
	interface WrongType {}
}
