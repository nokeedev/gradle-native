package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import dev.nokee.internal.testing.utils.TestUtils;
import dev.nokee.model.internal.core.ModelProjection;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.*;

@Subject(ManagedModelProjection.class)
class ManagedModelProjectionTest extends TypeCompatibilityModelProjectionSupportTest {
	@Test
	void managedProjectionCannotBeUsedAsIs() {
		val projection = ManagedModelProjection.of(of(MyType.class));
		assertThrows(UnsupportedOperationException.class, () -> projection.get(of(MyType.class)));
	}

	@Test
	void canBindProjectionToAnInstantiator() {
		assertAll(() -> {
			val projection = ManagedModelProjection.of(of(MyType.class)).bind(TestUtils.objectFactory()::newInstance);
			assertTrue(projection.canBeViewedAs(of(MyType.class)));
			assertFalse(projection.canBeViewedAs(of(WrongType.class)));

			assertThat(projection.get(of(MyType.class)), isA(MyType.class));
		});
	}

	@Override
	protected ModelProjection createSubject(Class<?> type) {
		return ManagedModelProjection.of(of(type));
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
			.addEqualityGroup(ManagedModelProjection.of(of(MyType.class)), ManagedModelProjection.of(of(MyType.class)))
			.addEqualityGroup(ManagedModelProjection.of(of(MyOtherType.class)))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(ManagedModelProjection.of(of(MyType.class)),
			hasToString("ManagedModelProjection.of(interface dev.nokee.model.internal.registry.ManagedModelProjectionTest$MyType)"));
	}

	interface MyType {}
	interface MyOtherType {}
	interface WrongType {}
}
