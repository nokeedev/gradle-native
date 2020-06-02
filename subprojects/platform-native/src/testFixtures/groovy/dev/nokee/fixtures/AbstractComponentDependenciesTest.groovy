package dev.nokee.fixtures

import dev.nokee.platform.base.internal.NamingScheme
import dev.nokee.runtime.nativebase.internal.LibraryElements
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.Usage
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers
import spock.lang.Specification
import spock.lang.Unroll

import static dev.nokee.fixtures.CollectionTestFixture.one
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE
import static dev.nokee.runtime.nativebase.internal.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import static org.junit.Assert.assertThat

abstract class AbstractComponentDependenciesTest extends Specification {
	def project = ProjectBuilder.builder().build()

	protected newDependencies(String variant = '') {
		def names = Mock(NamingScheme) {
			getConfigurationName(_) >> { args ->
				if (variant.empty) {
					return args[0]
				}
				return "${variant}${args[0].toString().capitalize()}"
			}
		}
		return project.objects.newInstance(getDependencyType(), names)
	}

	protected abstract Class getDependencyType()

	protected abstract List<String> getBucketsUnderTest()

	def "creates dependency buckets"() {
		given:
		newDependencies()

		expect:
		assertThat(project.configurations*.name, Matchers.containsInAnyOrder(*bucketsUnderTest))

		and:
		project.configurations.each {
			assertIsDependencyBucket(it)
		}
	}

	//region Dependency declaration
	@Unroll
	def "can declare external #bucketName dependencies as #notationType"(bucketName, notation, notationType) {
		given:
		def dependencies = newDependencies()

		when:
		dependencies."${bucketName}"(notation)

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ExternalDependency
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '1.0'
		dependency.attributes.empty

		where:
		[bucketName, notation, notationType] << withExternalDependencyNotation(bucketsUnderTest)
	}

	@Unroll
	def "can declare project #bucketName dependencies"(bucketName) {
		given:
		def dependencies = newDependencies()

		and:
		def foo = ProjectBuilder.builder().withParent(project).withName('foo').build()
		foo.group = 'com.example'
		foo.version = '1.0'

		when:
		dependencies."${bucketName}"(foo)

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ProjectDependency
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '1.0'
		dependency.attributes.empty

		where:
		bucketName << bucketsUnderTest
	}
	//endregion

	//region Dependency configuration
	@Unroll
	def "can configure project #bucketName dependencies"(bucketName) {
		given:
		def dependencies = newDependencies()

		and:
		def foo = ProjectBuilder.builder().withParent(project).withName('foo').build()
		foo.group = 'com.example'
		foo.version = '1.0'

		when:
		def configuredDependencies = []
		dependencies."${bucketName}"(foo) {
			configuredDependencies << it
			attributes {
				attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, 'some-usage'))
			}
		}

		then:
		def dependency = one(configuredDependencies)
		dependency instanceof ProjectDependency
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '1.0'
		dependency.attributes.keySet()*.name == [Usage.USAGE_ATTRIBUTE.name]
		dependency.attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name == 'some-usage'

		where:
		bucketName << bucketsUnderTest
	}

	@Unroll
	def "can configure external #bucketName dependencies as #notationType"(bucketName, notation, notationType) {
		given:
		def dependencies = newDependencies()

		when:
		def configuredDependencies = []
		dependencies."${bucketName}"(notation) {
			configuredDependencies << it
			attributes {
				attribute(Usage.USAGE_ATTRIBUTE, project.objects.named(Usage, 'some-usage'))
			}
		}

		then:
		def dependency = one(configuredDependencies)
		dependency instanceof ExternalDependency
		dependency.group == 'com.example'
		dependency.name == 'foo'
		dependency.version == '1.0'
		dependency.attributes.keySet()*.name == [Usage.USAGE_ATTRIBUTE.name]
		dependency.attributes.getAttribute(Usage.USAGE_ATTRIBUTE).name == 'some-usage'

		where:
		[bucketName, notation, notationType] << withExternalDependencyNotation(bucketsUnderTest)
	}
	//endregion

	@Unroll
	def "ensure type safety of the additional #bucketName dependency configuration"(bucketName) {
		given:
		def dependencies = newDependencies()

		and:
		def foo = ProjectBuilder.builder().withParent(project).withName('foo').build()
		foo.group = 'com.example'
		foo.version = '1.0'

		and:
		def configuredDependencies = []

		when:
		dependencies."${bucketName}"(foo, new Action<ExternalDependency>() {
			@Override
			void execute(ExternalDependency it) {
				configuredDependencies << it
			}
		})
		then:
		def ex1 = thrown(ClassCastException)
		ex1.message == 'org.gradle.api.internal.artifacts.dependencies.DefaultProjectDependency_Decorated cannot be cast to org.gradle.api.artifacts.ExternalDependency'
		and:
		configuredDependencies == []

		when:
		dependencies."${bucketName}"('com.example:foo:1.0', new Action<ProjectDependency>() {
			@Override
			void execute(ProjectDependency it) {
				configuredDependencies << it
			}
		})
		then:
		def ex2 = thrown(ClassCastException)
		ex2.message == 'org.gradle.api.internal.artifacts.dependencies.DefaultExternalModuleDependency_Decorated cannot be cast to org.gradle.api.artifacts.ProjectDependency'
		and:
		configuredDependencies == []

		where:
		bucketName << bucketsUnderTest
	}

	@Unroll
	def "can access underlying configuration of #bucketName bucket"(bucketName) {
		given:
		def dependencies = newDependencies()

		expect:
		dependencies."${bucketName}Dependencies" == project.configurations."${bucketName}"

		where:
		bucketName << bucketsUnderTest
	}

	def "can extend from other dependencies"() {
		given:
		def childDependencies = newDependencies('child')
		def parentDependencies = newDependencies('parent')

		when:
		childDependencies.extendsFrom(parentDependencies)

		then:
		bucketsUnderTest.each {
			assert childDependencies."${it}Dependencies".getExtendsFrom().contains(parentDependencies."${it}Dependencies")
		}
	}

	def "uses naming scheme for all dependency bucket name"() {
		when:
		newDependencies('foo')

		then:
		project.configurations*.name as Set == bucketsUnderTest.collect { "foo${it.capitalize()}" } as Set
	}

	protected void assertIsDependencyBucket(Configuration c) {
		assert !c.canBeResolved
		assert !c.canBeConsumed
	}

	def withExternalDependencyNotation(List<String> buckets) {
		def result = []
		buckets.each { bucketName ->
			result << [bucketName, [group: 'com.example', name: 'foo', version: '1.0'], 'map notation']
			result << [bucketName, 'com.example:foo:1.0', 'string notation']
		}
		return result
	}
}

/**
 * Tests specifically for allowing local darwin framework dependencies via the magic group.
 */
abstract class AbstractLocalDarwinFrameworkDependenciesTest extends Specification {
	def project = ProjectBuilder.builder().build()
	def names = Mock(NamingScheme) {
		getConfigurationName(_) >> { args -> args[0]}
	}
	def dependencies = project.objects.newInstance(getDependencyType(), names)

	protected abstract Class getDependencyType()

	protected abstract List<String> getBucketsUnderTest()

	@Unroll
	def "can declare local darwin framework #bucketName dependencies using magic group as #notationType"(bucketName, notation, notationType) {
		when:
		dependencies."${bucketName}"(notation)

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ExternalDependency
		dependency.group == 'dev.nokee.framework'
		dependency.name == 'Foundation'
		dependency.version == 'latest.release'
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'deserialized'

		where:
		[bucketName, notation, notationType] << withExternalDependencyNotation(bucketsUnderTest)
	}

	@Unroll
	def "can declare local darwin framework #bucketName dependencies using magic group as #notationType with configuration action"(bucketName, notation, notationType) {
		when:
		dependencies."${bucketName}"(notation) {
			// configuration action
		}

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ExternalDependency
		dependency.group == 'dev.nokee.framework'
		dependency.name == 'Foundation'
		dependency.version == 'latest.release'
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'deserialized'

		where:
		[bucketName, notation, notationType] << withExternalDependencyNotation(bucketsUnderTest)
	}

	def withExternalDependencyNotation(List<String> buckets) {
		def result = []
		buckets.each { bucketName ->
			result << [bucketName, [group: 'dev.nokee.framework', name: 'Foundation', version: 'latest.release'], 'map notation']
			result << [bucketName, 'dev.nokee.framework:Foundation:latest.release', 'string notation']
		}
		return result
	}

	@Unroll
	def "does not interfere with invalid map notation on #bucketName dependencies"(bucketName) {
		when: 'missing group'
		dependencies."${bucketName}"(name: 'Foundation', version: 'latest.release')

		then:
		noExceptionThrown()

		and:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ExternalDependency
		dependency.group == null
		dependency.name == 'Foundation'
		dependency.version == 'latest.release'
		dependency.attributes.empty

		where:
		bucketName << bucketsUnderTest
	}

	@Unroll
	def "ignores project #bucketName dependencies using magic group"(bucketName) {
		given:
		def foo = ProjectBuilder.builder().withParent(project).withName('foo').build()
		foo.group = 'dev.nokee.framework'
		foo.version = '1.0'

		when:
		dependencies."${bucketName}"(foo)

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ProjectDependency
		dependency.group == 'dev.nokee.framework'
		dependency.name == 'foo'
		dependency.version == '1.0'
		dependency.attributes.empty

		where:
		bucketName << bucketsUnderTest
	}

	@Unroll
	def "can configure local darwin framework #bucketName dependencies as #notationType"(bucketName, notation, notationType) {
		given:
		def kAttribute = Attribute.of('com.example.attribute', String)
		when:
		dependencies."${bucketName}"(notation) {
			attributes {
				attribute(kAttribute, 'foo')
			}
		}

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ExternalDependency
		dependency.group == 'dev.nokee.framework'
		dependency.name == 'Foundation'
		dependency.version == 'latest.release'
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType', 'com.example.attribute'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'deserialized'
		dependency.attributes.getAttribute(kAttribute) == 'foo'

		where:
		[bucketName, notation, notationType] << withExternalDependencyNotation(bucketsUnderTest)
	}

	@Unroll
	def "can overwrite local darwin framework configuration for #bucketName dependencies as #notationType"(bucketName, notation, notationType) {
		when:
		dependencies."${bucketName}"(notation) {
			attributes {
				attribute(LIBRARY_ELEMENTS_ATTRIBUTE, project.objects.named(LibraryElements, 'my-framework-bundle'))
				attribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, 'my-deserialized')
			}
		}

		then:
		def dependency = one(project.configurations."${bucketName}".dependencies)
		dependency instanceof ExternalDependency
		dependency.group == 'dev.nokee.framework'
		dependency.name == 'Foundation'
		dependency.version == 'latest.release'
		dependency.attributes.keySet()*.name as Set == ['org.gradle.libraryelements', 'dev.nokee.artifactSerializationType'] as Set
		dependency.attributes.getAttribute(LIBRARY_ELEMENTS_ATTRIBUTE).name == 'my-framework-bundle'
		dependency.attributes.getAttribute(ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE) == 'my-deserialized'

		where:
		[bucketName, notation, notationType] << withExternalDependencyNotation(bucketsUnderTest)
	}
}

abstract class AbstractLibraryDependenciesTest extends Specification {
	def project = ProjectBuilder.builder().build()
	def names = Mock(NamingScheme) {
		getConfigurationName(_) >> { args -> args[0]}
	}
	def dependencies = project.objects.newInstance(getDependencyType(), names)

	protected abstract Class getDependencyType()

	protected abstract String getApiBucketNameUnderTest()

	protected abstract String getImplementationBucketNameUnderTest()

	def "implementation bucket extends from api bucket"() {
		expect:
		dependencies."${implementationBucketNameUnderTest}Dependencies".getExtendsFrom() == [dependencies."${apiBucketNameUnderTest}Dependencies"] as Set
	}
}
