package dev.nokee.testing.base;

import org.gradle.api.Named;

public interface TestSuiteComponent extends Named {
	TestSuiteComponent testedComponent(Object component);
}
