package dev.nokee.platform.jni.internal

import dev.nokee.fixtures.AbstractComponentDependenciesTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies
import spock.lang.Subject

@Subject(JniLibraryNativeDependenciesInternal)
class JniLibraryNativeDependenciesInternalTest extends AbstractComponentDependenciesTest {
	@Override
	protected newDependencies(String variant) {
		def nativeNames = variant.empty ? super.newNamingScheme('native') : super.newNamingScheme("${variant}Native")
		def names = super.newNamingScheme(variant)
		def buckets = project.objects.newInstance(DefaultNativeComponentDependencies, nativeNames)
		return project.objects.newInstance(JniLibraryNativeDependenciesInternal, buckets, Mock(NativeIncomingDependencies))
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

@Subject(JniLibraryNativeDependenciesInternal)
class JniLibraryLocalDarwinFrameworkNativeDependenciesTest extends AbstractLocalDarwinFrameworkDependenciesTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def nativeNames = Mock(NamingScheme) {
			getConfigurationName(_) >> { args -> "native${args[0].capitalize()}" }
		}
		def buckets = project.objects.newInstance(DefaultNativeComponentDependencies, nativeNames)
		return project.objects.newInstance(JniLibraryNativeDependenciesInternal, buckets, Mock(NativeIncomingDependencies))
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}
