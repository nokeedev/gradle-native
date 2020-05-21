package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import dev.nokee.platform.nativebase.internal.DefaultNativeComponentDependencies
import spock.lang.Subject

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesTest extends AbstractComponentDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return DefaultNativeComponentDependencies
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentLocalDarwinFrameworkDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return DefaultNativeComponentDependencies
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}
