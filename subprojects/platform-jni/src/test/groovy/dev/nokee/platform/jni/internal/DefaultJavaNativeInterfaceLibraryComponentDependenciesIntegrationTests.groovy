package dev.nokee.platform.jni.internal

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLibraryComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory

@Subject(DefaultJavaNativeInterfaceLibraryComponentDependencies)
class DefaultJavaNativeInterfaceLibraryComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
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
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
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
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
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
