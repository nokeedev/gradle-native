package dev.nokee.platform.base.internal.dependencies

import dev.nokee.platform.base.AbstractComponentDependenciesDelegateTest
import spock.lang.Subject

@Subject(BaseComponentDependencies)
class BaseComponentDependenciesTest extends AbstractComponentDependenciesDelegateTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new BaseComponentDependencies(delegate)
	}
}
