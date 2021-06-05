package dev.nokee.runtime.core;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateTestUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

public interface CoordinateFactoryTester {
	<T> Coordinate<T> createSubject(CoordinateAxis<T> axis, T value);

	@Test
	default void checkAxis() {
		assertThat(createSubject(xAxis(), 4L).getAxis(), equalTo(xAxis()));
		assertThat(createSubject(yAxis(), 3L).getAxis(), equalTo(yAxis()));
	}

	@Test
	default void checkValue() {
		assertThat(createSubject(xAxis(), 4L).getValue(), is(4L));
		assertThat(createSubject(yAxis(), 3L).getValue(), is(3L));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullConstruction() throws NoSuchMethodException {
		new NullPointerTester()
			.setDefault(CoordinateAxis.class, testAxis())
			.testMethod(this, CoordinateFactoryTester.class.getDeclaredMethod("createSubject", CoordinateAxis.class, Object.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(xAxis(), 1L), createSubject(xAxis(), 1L))
			.addEqualityGroup(createSubject(yAxis(), 1L))
			.addEqualityGroup(createSubject(xAxis(), 2L))
			.testEquals();
	}
}
