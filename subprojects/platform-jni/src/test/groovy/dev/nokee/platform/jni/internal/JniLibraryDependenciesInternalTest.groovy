package dev.nokee.platform.jni.internal

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLibraryDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import spock.lang.Subject

@Subject(JniLibraryDependenciesInternal)
class JniLibraryDependenciesInternalTest extends AbstractComponentDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return JniLibraryDependenciesInternal
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'jvmImplementation', 'jvmRuntimeOnly', 'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

@Subject(JniLibraryDependenciesInternal)
class JniLibraryLocalDarwinFrameworkDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return JniLibraryDependenciesInternal
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

// TODO: api and jvmImplementation and jvmRuntimeOnly are not local darwin framework aware

class JniLibraryLibraryDependenciesTest extends AbstractLibraryDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return JniLibraryDependenciesInternal
	}

	@Override
	protected String getApiBucketNameUnderTest() {
		return 'api'
	}

	@Override
	protected String getImplementationBucketNameUnderTest() {
		return 'jvmImplementation'
	}
}
