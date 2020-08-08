package dev.nokee.platform.nativebase

import dev.nokee.fixtures.AbstractComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLibraryComponentDependenciesIntegrationTest
import dev.nokee.fixtures.AbstractLocalDarwinFrameworkDependenciesIntegrationTest
import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.platform.base.internal.dependencies.ConfigurationFactories
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyFactory
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory
import spock.lang.Subject

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesIntegrationTest extends AbstractComponentDependenciesIntegrationTest {
	@Override
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
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
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
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
	protected newDependencies(NamingScheme names) {
		def dependencyContainer = project.objects.newInstance(DefaultComponentDependencies, "test component", new FrameworkAwareDependencyBucketFactory(new DefaultDependencyBucketFactory(new ConfigurationFactories.Prefixing(new ConfigurationFactories.Creating(project.configurations), { target -> names.getConfigurationName(target) }), new DefaultDependencyFactory(project.dependencies))))
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
