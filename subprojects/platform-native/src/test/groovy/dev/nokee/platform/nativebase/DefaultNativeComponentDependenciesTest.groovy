package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies
import spock.lang.Subject

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesTest extends AbstractComponentDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		return project.objects.newInstance(DefaultNativeComponentDependencies, names)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentLocalDarwinFrameworkDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		return project.objects.newInstance(DefaultNativeComponentDependencies, names)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}
