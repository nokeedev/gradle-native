package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory

@Subject(DefaultNativeApplicationComponentDependencies)
class DefaultNativeApplicationComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeApplicationComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeApplicationComponentDependencies)
class DefaultNativeApplicationComponentDependenciesLocalDarwinFrameworkIntegrationTest extends AbstractLocalDarwinFrameworkDependenciesIntegrationTest {
	@Override
	protected newDependencies(DomainObjectIdentifierInternal identifier) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(objectFactory(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeApplicationComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}
