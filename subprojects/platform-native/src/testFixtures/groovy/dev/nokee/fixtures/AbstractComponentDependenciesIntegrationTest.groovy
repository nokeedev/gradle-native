/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.fixtures

import dev.nokee.internal.testing.util.ProjectTestUtils
import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ExternalDependency
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.attributes.Attribute
import org.gradle.api.attributes.LibraryElements
import org.gradle.api.attributes.Usage
import org.gradle.testfixtures.ProjectBuilder
import org.hamcrest.Matchers
import spock.lang.Ignore
import spock.lang.Specification
import spock.lang.Unroll

import static dev.nokee.fixtures.CollectionTestFixture.one
import static dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE
import static org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import static org.junit.Assert.assertThat

abstract class AbstractComponentDependenciesIntegrationTest extends Specification {
	def project = ProjectTestUtils.rootProject()

	protected DomainObjectIdentifier newNamingScheme(String variant = '') {
		return VariantIdentifier.of(variant, Variant, ComponentIdentifier.ofMain(ProjectIdentifier.of(project)))
	}

	protected newDependencies(String variant = '') {
		return newDependencies(newNamingScheme(variant))
	}
	protected abstract newDependencies(DomainObjectIdentifier identifier)

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
		dependencies."${bucketName}".asConfiguration == project.configurations.getByName(bucketName)

		where:
		bucketName << bucketsUnderTest
	}

	@Ignore("Extending dependencies is important but the way to do it changed")
	def "can extend from other dependencies"() {
		given:
		def childDependencies = newDependencies('child')
		def parentDependencies = newDependencies('parent')

		when:
		childDependencies.extendsFrom(parentDependencies)

		then:
		bucketsUnderTest.each {
			assert childDependencies."${it}".asConfiguration.getExtendsFrom().contains(parentDependencies."${it}".asConfiguration)
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
abstract class AbstractLocalDarwinFrameworkDependenciesIntegrationTest extends Specification {
	def project = ProjectTestUtils.rootProject()
	def identifier = ComponentIdentifier.ofMain(ProjectIdentifier.of(project))
	def dependencies = newDependencies(identifier)

	protected abstract newDependencies(DomainObjectIdentifier identifier)

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

abstract class AbstractLibraryComponentDependenciesIntegrationTest extends Specification {
	def project = ProjectTestUtils.rootProject()
	def identifier = ComponentIdentifier.ofMain(ProjectIdentifier.of(project))
	def dependencies = newDependencies(identifier)

	protected abstract newDependencies(DomainObjectIdentifier identifier)

	protected abstract String getApiBucketNameUnderTest()

	protected abstract String getImplementationBucketNameUnderTest()

	def "implementation bucket extends from api bucket"() {
		expect:
		dependencies."${implementationBucketNameUnderTest}".asConfiguration.getExtendsFrom() == [dependencies."${apiBucketNameUnderTest}".asConfiguration] as Set
	}
}
