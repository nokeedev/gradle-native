package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryDependencies
import spock.lang.Subject

@Subject(DefaultNativeLibraryDependencies)
class DefaultNativeLibraryDependenciesTest extends AbstractComponentDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return DefaultNativeLibraryDependencies
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryDependencies)
class DefaultNativeLibraryLocalDarwinFrameworkDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return DefaultNativeLibraryDependencies
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}
