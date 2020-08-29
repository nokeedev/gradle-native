package dev.nokee.platform.base.internal.dependencies


import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(PlatformBaseDependenciesModule)
class PlatformBaseDependenciesModuleIntegrationTest extends Specification {
	def "registers the three basic bucket type factories"() {
		given:
		def project = ProjectBuilder.builder().build()
		def component = DaggerTestableInstantiatorComponent.factory().create(project)

		when:
		def instantiator = component.instantiator()

		then:
		instantiator.getCreatableTypes() == [ResolvableDependencies, DeclarableDependencies, ConsumableDependencies] as Set
	}

	def "can bind more bucket types using dagger"() {
		given:
		def project = ProjectBuilder.builder().build()
		def component = DaggerTestableCustomDependencyBucketAwareInstantiatorComponent.factory().create(project)

		when:
		def instantiator = component.instantiator()

		then:
		instantiator.getCreatableTypes() == [ResolvableDependencies, DeclarableDependencies, ConsumableDependencies, MyCustomDependencyBucket] as Set
	}
}
