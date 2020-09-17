package dev.nokee.testing.base;

import dev.nokee.platform.base.internal.Component;
import org.gradle.api.Named;

public interface TestSuiteComponent extends Named, Component {
	TestSuiteComponent testedComponent(Object component);
}
