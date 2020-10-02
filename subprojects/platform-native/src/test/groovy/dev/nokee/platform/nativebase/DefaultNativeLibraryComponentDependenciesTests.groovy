package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLibraryComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesLocalDarwinFrameworkIntegrationTest extends AbstractLocalDarwinFrameworkDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeLibraryComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['api', 'implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesApiBucketIntegrationTest extends AbstractLibraryComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeLibraryComponentDependencies, dependencyContainer)
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
