package dev.nokee.platform.jni.internal

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLibraryComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory
import dev.nokee.platform.base.internal.dependencies.PrefixingNamingSchemes
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

@Subject(DefaultJavaNativeInterfaceLibraryComponentDependencies)
class DefaultJavaNativeInterfaceLibraryComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(String variant) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), PrefixingNamingSchemes.of(variant)), new DefaultDependencyFactory(project.dependencies))))
		return project.objects.newInstance(DefaultJavaNativeInterfaceLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'jvmImplementation', 'jvmRuntimeOnly', 'nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

@Subject(DefaultJavaNativeInterfaceLibraryComponentDependencies)
class DefaultJavaNativeInterfaceLibraryComponentDependenciesLocalDarwinFrameworkIntegrationTest extends AbstractLocalDarwinFrameworkDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
		return project.objects.newInstance(DefaultJavaNativeInterfaceLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['nativeImplementation', 'nativeLinkOnly', 'nativeRuntimeOnly']
	}
}

// TODO: api and jvmImplementation and jvmRuntimeOnly are not local darwin framework aware

class DefaultJavaNativeInterfaceLibraryComponentDependenciesApiBucketIntegrationTest extends AbstractLibraryComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
		return project.objects.newInstance(DefaultJavaNativeInterfaceLibraryComponentDependencies, dependencyContainer)
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
