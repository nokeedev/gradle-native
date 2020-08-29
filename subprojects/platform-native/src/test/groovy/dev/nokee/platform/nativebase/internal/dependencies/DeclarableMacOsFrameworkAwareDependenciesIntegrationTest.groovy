package dev.nokee.platform.nativebase.internal.dependencies

import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryFactory
import dev.nokee.platform.base.internal.dependencies.DependencyIdentifier
import dev.nokee.runtime.nativebase.internal.LibraryElements
import org.gradle.api.Action
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE
import static dev.nokee.runtime.nativebase.internal.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE

class DeclarableMacOsFrameworkAwareDependenciesIntegrationTest extends Specification {
	def project = ProjectBuilder.builder().build()
	def identifier = DependencyIdentifier.of(DependencyBucketName.of('foo'), DeclarableMacOsFrameworkAwareDependencies, ProjectIdentifier.of(project))

	private DeclarableMacOsFrameworkAwareDependencies newSubject() {
		return DependencyBucketFactoryFactory.create(identifier, project.configurations.create('foo'), project.dependencies, { new DeclarableMacOsFrameworkAwareDependencies() })
	}

	private static <T> T one(Iterable<T> iterable) {
		assert iterable.size() == 1
		return iterable.first()
	}

	def "can add non-macOS framework dependencies"() {
		given:
		def subject = newSubject()

		when:
		subject.addDependency('com.example:foo:4.2')

		then:
		def dependency = (ModuleDependency)one(project.configurations.foo.dependencies)
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '4.2'
		dependency.attributes.empty
		dependency.requestedCapabilities == []
	}

	def "can add non-macOS framework dependencies with configuration action"() {
		given:
		def subject = newSubject()

		when:
		subject.addDependency('com.example:foo:4.2', Stub(Action))

		then:
		def dependency = (ModuleDependency)one(project.configurations.foo.dependencies)
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '4.2'
		dependency.attributes.empty
		dependency.requestedCapabilities == []
	}

	@Unroll
	def "adds additional configuration when magic group is present"(notation) {
		given:
		def subject = newSubject()

		when:
		subject.addDependency(notation)

		then:
		def dependency = (ModuleDependency)one(project.configurations.foo.dependencies)
		!dependency.attributes.empty
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'deserialized'

		where:
		notation << [[group: 'dev.nokee.framework', name: 'foo', version: '4.2'], 'dev.nokee.framework:foo:4.2']
	}

	@Unroll
	def "adds additional configuration when magic group is present as composite action"(notation) {
		given:
		def subject = newSubject()

		when:
		subject.addDependency(notation, Stub(Action))

		then:
		def dependency = (ModuleDependency)one(project.configurations.foo.dependencies)
		!dependency.attributes.empty
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'deserialized'

		where:
		notation << [[group: 'dev.nokee.framework', name: 'foo', version: '4.2'], 'dev.nokee.framework:foo:4.2']
	}

	def "can overwrite framwork attributes"() {
		given:
		def subject = newSubject()
		def notation = 'dev.nokee.framework:foo:4.2'

		when:
		subject.addDependency(notation, new Action<ModuleDependency>() {
			@Override
			void execute(ModuleDependency moduleDependency) {
				moduleDependency.attributes {
					attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, 'my-framework-bundle'))
					attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, 'my-deserialized')
				}
			}
		})
		then:
		def dependency = (ModuleDependency)one(project.configurations.foo.dependencies)
		!dependency.attributes.empty
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'my-framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'my-deserialized'
	}
}
