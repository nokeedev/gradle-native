package dev.nokee.platform.jni.internal

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import spock.lang.Subject

@Subject(JniLibraryNativeDependenciesInternal)
class JniLibraryNativeDependenciesInternalTest extends AbstractComponentDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return JniLibraryNativeDependenciesInternal
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

@Subject(JniLibraryNativeDependenciesInternal)
class JniLibraryLocalDarwinFrameworkNativeDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected Class getDependencyType() {
		return JniLibraryNativeDependenciesInternal
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}
