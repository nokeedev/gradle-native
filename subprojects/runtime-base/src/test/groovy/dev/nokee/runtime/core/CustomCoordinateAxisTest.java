package dev.nokee.runtime.core;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CustomCoordinateAxisTest implements CoordinateAxisTester<TestAxis> {
	@Override
	public CoordinateAxis<TestAxis> createSubject() {
		return new CustomAxis();
	}

	@Test
	void defaultImplementationInfersNameFromType() {
		assertThat(createSubject().getName(), equalTo("test-axis"));
	}

	protected static final class CustomAxis implements CoordinateAxis<TestAxis> {
		@Override
		public Class<TestAxis> getType() {
			return TestAxis.class;
		}
	}
}
