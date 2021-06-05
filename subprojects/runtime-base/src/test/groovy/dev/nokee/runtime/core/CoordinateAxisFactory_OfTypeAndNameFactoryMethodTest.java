package dev.nokee.runtime.core;

import dev.nokee.internal.testing.Assumptions;

class CoordinateAxisFactory_OfTypeAndNameFactoryMethodTest implements CoordinateAxisTester<TestAxis>, CoordinateAxisFactoryTester {
	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type) {
		return Assumptions.skipCurrentTestExecution("Testing CoordinateAxis.of(Class, String) factory method.");
	}

	@Override
	public <T> CoordinateAxis<T> createSubject(Class<T> type, String name) {
		return CoordinateAxis.of(type, name);
	}

	@Override
	public CoordinateAxis<TestAxis> createSubject() {
		return CoordinateAxis.of(TestAxis.class, "test");
	}
}
