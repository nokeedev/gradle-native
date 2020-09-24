package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def identifier = VariantIdentifier.of(names.getConfigurationName(''), Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesLocalDarwinFrameworkIntegrationTest extends AbstractLocalDarwinFrameworkDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def identifier = VariantIdentifier.of(names.getConfigurationName(''), Variant, ComponentIdentifier.ofMain(Component, ProjectIdentifier.of('root')))
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, identifier, new FrameworkAwareDependencyBucketFactory(new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)))
		return project.objects.newInstance(DefaultNativeComponentDependencies, dependencyContainer)
	}

	@Override
	protected List<String> getBucketsUnderTest() {
		return ['implementation', 'linkOnly', 'runtimeOnly', 'compileOnly']
	}
}
