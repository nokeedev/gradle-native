package dev.nokee.platform.jni.internal

import dev.nokee.platform.base.AbstractBaseComponentDependenciesContainerTest
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainerImpl
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInstantiator
import dev.nokee.platform.nativebase.internal.dependencies.DeclarableMacOsFrameworkAwareDependencies
import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Subject

@Subject(DefaultJavaNativeInterfaceNativeComponentDependencies)
class DefaultJavaNativeInterfaceNativeComponentDependenciesTest extends AbstractBaseComponentDependenciesContainerTest<DefaultJavaNativeInterfaceNativeComponentDependencies> {
	@Override
	protected DefaultJavaNativeInterfaceNativeComponentDependencies newSubject(ComponentDependenciesContainer delegate) {
		return new DefaultJavaNativeInterfaceNativeComponentDependencies(delegate)
	}

	@Override
	protected Set<String> getPredefinedDeclarableBucketNames() {
		return ['nativeImplementation', 'nativeRuntimeOnly', 'nativeLinkOnly']
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
		configurations.nativeLinkOnly.extendsFrom == [configurations.nativeImplementation] as Set
		configurations.nativeRuntimeOnly.extendsFrom == [configurations.nativeImplementation] as Set

		and:
		subject.nativeImplementation instanceof DeclarableMacOsFrameworkAwareDependencies
		subject.nativeRuntimeOnly instanceof DeclarableMacOsFrameworkAwareDependencies
		subject.nativeLinkOnly instanceof DeclarableMacOsFrameworkAwareDependencies
	}
}
