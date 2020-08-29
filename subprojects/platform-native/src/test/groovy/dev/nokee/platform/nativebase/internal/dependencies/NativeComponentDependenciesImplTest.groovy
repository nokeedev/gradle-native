package dev.nokee.platform.nativebase.internal.dependencies

import dev.nokee.platform.base.AbstractBaseComponentDependenciesContainerTest
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerImpl
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInstantiator
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Subject

@Subject(NativeComponentDependenciesImpl)
class NativeComponentDependenciesImplTest extends AbstractBaseComponentDependenciesContainerTest<NativeComponentDependenciesImpl> {
	@Override
	protected NativeComponentDependenciesImpl newSubject(ComponentDependenciesContainer delegate) {
		return new NativeComponentDependenciesImpl(delegate)
	}

	@Override
	protected Set<String> getPredefinedDeclarableBucketNames() {
		return ['implementation', 'compileOnly', 'runtimeOnly', 'linkOnly']
	}

	@Override
	protected DependencyBucketInstantiator newInstantiator(Project project) {
		return DaggerTestableComponents.factory().create(project).instantiator()
	}

	def "configures predefined buckets"() {
		given:
		def project = ProjectBuilder.builder().build()
		def configurations = project.configurations
		def instantiator = newInstantiator(project)

		when:
		def subject = newSubject(new ComponentDependenciesContainerImpl(ProjectIdentifier.of(project), project.configurations, instantiator))

		then:
		configurations.compileOnly.extendsFrom == [configurations.implementation] as Set
		configurations.linkOnly.extendsFrom == [configurations.implementation] as Set
		configurations.runtimeOnly.extendsFrom == [configurations.implementation] as Set

		and:
		subject.implementation instanceof DeclarableMacOsFrameworkAwareDependencies
		subject.compileOnly instanceof DeclarableMacOsFrameworkAwareDependencies
		subject.linkOnly instanceof DeclarableMacOsFrameworkAwareDependencies
		subject.runtimeOnly instanceof DeclarableMacOsFrameworkAwareDependencies
	}
}
