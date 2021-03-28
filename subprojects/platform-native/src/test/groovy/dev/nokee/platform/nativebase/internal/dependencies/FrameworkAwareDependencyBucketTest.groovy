package dev.nokee.platform.nativebase.internal.dependencies

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import dev.nokee.runtime.nativebase.internal.LibraryElements
import org.gradle.api.Action
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.ModuleDependency
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE
import static dev.nokee.runtime.nativebase.internal.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE

@Subject(FrameworkAwareDependencyBucket)
class FrameworkAwareDependencyBucketTest extends Specification {
	def delegate = Mock(dev.nokee.platform.base.DependencyBucket)
	def subject = new FrameworkAwareDependencyBucket(delegate)

	def "forwards getName() to delegate"() {
		when:
		subject.name

		then:
		1 * delegate.getName()
		0 * _
	}

	def "forwards addDependency(Object) to delegate"() {
		given:
		def notation = new Object()

		when:
		subject.addDependency(notation)

		then:
		1 * delegate.addDependency(notation)
		0 * _
	}

	def "forwards addDependency(Object, Action) to delegate"() {
		given:
		def notation = new Object()
		def action = Mock(Action)

		when:
		subject.addDependency(notation, action)

		then:
		1 * delegate.addDependency(notation, action)
		0 * _
	}

	def "forwards getAsConfiguration() to delegate"() {
		when:
		subject.asConfiguration

		then:
		1 * delegate.getAsConfiguration()
		0 * _
	}

	@Unroll
	def "adds additional configuration when magic group is present"(notation) {
		given:
		def project = ProjectTestUtils.rootProject()
		ExternalDependency dependency = (ExternalDependency) project.dependencies.create(notation)

		when:
		subject.addDependency(notation)
		then:
		1 * delegate.addDependency(notation, _) >> { args -> args[1].execute(dependency) }
		0 * _

		and:
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
		def project = ProjectTestUtils.rootProject()
		ExternalDependency dependency = (ExternalDependency) project.dependencies.create(notation)
		def action = Mock(Action)

		when:
		subject.addDependency(notation, action)
		then:
		1 * delegate.addDependency(notation, _) >> { args -> args[1].execute(dependency) }
		1 * action.execute(dependency)
		0 * _

		and:
		!dependency.attributes.empty
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'deserialized'

		where:
		notation << [[group: 'dev.nokee.framework', name: 'foo', version: '4.2'], 'dev.nokee.framework:foo:4.2']
	}

	def "can overwrite framwork attributes"() {
		given:
		def notation = 'dev.nokee.framework:foo:4.2'
		def project = ProjectTestUtils.rootProject()
		ExternalDependency dependency = (ExternalDependency) project.dependencies.create(notation)

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
		1 * delegate.addDependency(notation, _) >> { args -> args[1].execute(dependency) }
		0 * _

		and:
		!dependency.attributes.empty
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'my-framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'my-deserialized'
	}
}
