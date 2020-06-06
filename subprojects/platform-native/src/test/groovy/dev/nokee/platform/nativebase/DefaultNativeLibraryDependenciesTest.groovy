package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLibraryDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryDependencies
import spock.lang.Subject

@Subject(DefaultNativeLibraryDependencies)
class DefaultNativeLibraryDependenciesTest extends AbstractComponentDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		return project.objects.newInstance(DefaultNativeLibraryDependencies, names)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryDependencies)
class DefaultNativeLibraryLocalDarwinFrameworkDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		return project.objects.newInstance(DefaultNativeLibraryDependencies, names)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryDependencies)
class DefaultLibraryDependenciesTest extends AbstractLibraryDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		return project.objects.newInstance(DefaultNativeLibraryDependencies, names)
	}

	@Override
	protected String getApiBucketNameUnderTest() {
		return 'api'
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'implementation'
	}
}
