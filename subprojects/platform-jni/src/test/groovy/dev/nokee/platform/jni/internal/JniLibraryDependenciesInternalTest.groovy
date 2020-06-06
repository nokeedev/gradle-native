package dev.nokee.platform.jni.internal

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLibraryDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies
import spock.lang.Subject

@Subject(JniLibraryDependenciesInternal)
class JniLibraryDependenciesInternalTest extends AbstractComponentDependenciesTest {
	@Override
	protected newDependencies(String variant) {
		def nativeNames = variant.empty ? super.newNamingScheme('native') : super.newNamingScheme("${variant}Native")
		def names = super.newNamingScheme(variant)
		def buckets = project.objects.newInstance(DefaultNativeComponentDependencies, nativeNames)
		return project.objects.newInstance(JniLibraryDependenciesInternal, names, buckets)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		// TODO: 'nativeCompileOnly' should not be added here
		return ['api', 'jvmImplementation', 'jvmRuntimeOnly', 'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

@Subject(JniLibraryDependenciesInternal)
class JniLibraryLocalDarwinFrameworkDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def nativeNames = Mock(NamingScheme) {
			getConfigurationName(_) >> { args -> "native${args[0].capitalize()}" }
		}
		def buckets = project.objects.newInstance(DefaultNativeComponentDependencies, nativeNames)
		return project.objects.newInstance(JniLibraryDependenciesInternal, names, buckets)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

// TODO: api and jvmImplementation and jvmRuntimeOnly are not local darwin framework aware

class JniLibraryLibraryDependenciesTest extends AbstractLibraryDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def nativeNames = Mock(NamingScheme) {
			getConfigurationName(_) >> { args -> "native${args[0].capitalize()}" }
		}
		def buckets = project.objects.newInstance(DefaultNativeComponentDependencies, nativeNames)
		return project.objects.newInstance(JniLibraryDependenciesInternal, names, buckets)
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
