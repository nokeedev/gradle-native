package dev.nokee.testing.base;

import dev.nokee.platform.base.internal.Component;
import org.gradle.api.Named;
import org.gradle.api.provider.Property;

public interface TestSuiteComponent extends Component, Named {
	TestSuiteComponent testedComponent(Object component);
}
