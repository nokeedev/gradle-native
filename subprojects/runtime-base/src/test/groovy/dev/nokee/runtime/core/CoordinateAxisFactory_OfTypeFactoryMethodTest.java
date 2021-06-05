package dev.nokee.runtime.core;

import dev.nokee.internal.testing.Assumptions;

class CoordinateAxisFactory_OfTypeFactoryMethodTest implements CoordinateAxisTester<TestAxis>, CoordinateAxisFactoryTester {
	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type) {
		return CoordinateAxis.of(type);
	}

	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type, String name) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateAxis.of(Class) factory method.");
	}

	@Override
	public CoordinateAxis<TestAxis> createSubject() {
		return CoordinateAxis.of(TestAxis.class);
	}
}
