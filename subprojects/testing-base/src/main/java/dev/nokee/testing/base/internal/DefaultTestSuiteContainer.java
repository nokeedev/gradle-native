package dev.nokee.testing.base.internal;

import dev.nokee.model.internal.BaseNamedDomainObjectContainer;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;

import javax.inject.Inject;

public class DefaultTestSuiteContainer extends BaseNamedDomainObjectContainer<TestSuiteComponent> implements TestSuiteContainer {
	@Inject
	public DefaultTestSuiteContainer() {
		super(TestSuiteComponent.class);
	}
}
