package dev.nokee.testing.base;

import dev.nokee.platform.base.Component;

// FIXME: Don't extends from Component
public interface TestSuiteComponent extends Component {
	TestSuiteComponent testedComponent(Object component);
}
