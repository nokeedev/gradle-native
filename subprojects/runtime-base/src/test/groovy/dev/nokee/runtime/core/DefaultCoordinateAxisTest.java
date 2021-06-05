package dev.nokee.runtime.core;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertAll;

class DefaultCoordinateAxisTest implements CoordinateAxisTester<TestAxis>, CoordinateAxisFactoryTester {
	@Override
	public CoordinateAxis<TestAxis> createSubject() {
		return new DefaultCoordinateAxis<>(TestAxis.class, "test");
	}

	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type) {
		return new DefaultCoordinateAxis<>(type);
	}

	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type, String name) {
		return new DefaultCoordinateAxis<>(type, name);
	}

	@Test
	void checkToString() {
		assertAll(
			() -> assertThat(new DefaultCoordinateAxis<>(TestAxis.class),
				hasToString("axis <test-axis>")),
			() -> assertThat(new DefaultCoordinateAxis<>(TestAxis.class, "custom-name"),
				hasToString("axis <custom-name>"))
		);
	}
}
