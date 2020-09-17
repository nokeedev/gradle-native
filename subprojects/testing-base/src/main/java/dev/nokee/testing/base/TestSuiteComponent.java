package dev.nokee.testing.base;

import dev.nokee.platform.base.Component;
import org.gradle.api.Named;

// FIXME: Don't extends from Component
public interface TestSuiteComponent extends Named, Component {
	TestSuiteComponent testedComponent(Object component);
}
