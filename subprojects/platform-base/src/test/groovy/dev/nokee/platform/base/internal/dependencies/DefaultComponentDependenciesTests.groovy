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
package dev.nokee.platform.base.internal.dependencies

import dev.nokee.internal.testing.util.ProjectTestUtils
import dev.nokee.model.internal.ProjectIdentifier
import dev.nokee.platform.base.AbstractComponentDependenciesGroovyDslTest
import dev.nokee.platform.base.DependencyBucket
import org.gradle.api.UnknownDomainObjectException
import org.gradle.api.artifacts.Configuration
import spock.lang.Specification
import spock.lang.Subject

@Subject(DefaultComponentDependencies)
class DefaultComponentDependenciesTest extends Specification {
	def project = ProjectTestUtils.rootProject()
	def factory = Mock(DependencyBucketFactory)
	def ownerIdentifier = ProjectIdentifier.of('root')
	def dependencies = project.objects.newInstance(DefaultComponentDependencies, ownerIdentifier, factory)

	def "can create dependency bucket"() {
		when:
		dependencies.create('foo')

		then:
		1 * factory.create(DependencyBucketIdentifier.of(DependencyBucketName.of('foo'), DeclarableDependencyBucket, ownerIdentifier)) >> Mock(DependencyBucket)
		0 * _
	}

	def "can find missing and existing bucket"() {
		given:
		factory.create(_) >> Mock(DependencyBucket)

		and:
		def bucket = dependencies.create('foo')

		expect:
		dependencies.findByName('foo').present
		dependencies.findByName('foo').get() == bucket

		and:
		!dependencies.findByName('missing').present
	}

	def "can get existing bucket"() {
		given:
		factory.create(_) >> Mock(DependencyBucket)

		and:
		def bucket = dependencies.create('foo')

		expect:
		dependencies.getByName('foo') == bucket
	}

	def "throws exception when geting missing bucket"() {
		when:
		dependencies.getByName('missing')

		then:
		def ex = thrown(UnknownDomainObjectException)
		ex.message == "DependencyBucket with name 'missing' not found."
	}

	def "can create dependencies from dependencies bucket"() {
		given:
		def childFactory = Mock(DependencyBucketFactory)
		def childDependencies = project.objects.newInstance(DefaultComponentDependencies, ProjectIdentifier.of('root'), childFactory)

		and:
		factory.create(_) >> { DependencyBucketIdentifier identifier ->
			def name = identifier.name.get()
			return Mock(DependencyBucket) {
				getName() >> name
			}
		}

		and:
		dependencies.create('implementation')
		dependencies.create('compileOnly')

		when:
		dependencies.configureEach {
			childDependencies.create(it.name)
		}
		then:
		1 * childFactory.create(DependencyBucketIdentifier.of(DependencyBucketName.of('implementation'), DeclarableDependencyBucket, ProjectIdentifier.of('root'))) >> Mock(DependencyBucket)
		1 * childFactory.create(DependencyBucketIdentifier.of(DependencyBucketName.of('compileOnly'), DeclarableDependencyBucket, ProjectIdentifier.of('root'))) >> Mock(DependencyBucket)

		when:
		dependencies.create('runtimeOnly')
		then:
		1 * childFactory.create(DependencyBucketIdentifier.of(DependencyBucketName.of('runtimeOnly'), DeclarableDependencyBucket, ProjectIdentifier.of('root'))) >> Mock(DependencyBucket)
	}

	def "can extends matching dependencies between buckets"() {
		given:
		def childFactory = Mock(DependencyBucketFactory)
		def childDependencies = project.objects.newInstance(DefaultComponentDependencies, ProjectIdentifier.of('root'), childFactory)

		and:
		def childConfigurations = [implementation: Mock(Configuration), compileOnly: Mock(Configuration), runtimeOnly: Mock(Configuration), foo: Mock(Configuration)]
		childFactory.create(_) >> { DependencyBucketIdentifier identifier ->
			def name = identifier.name.get()
			Mock(DependencyBucket) {
				getName() >> name
				getAsConfiguration() >> { childConfigurations.get(name) }
			}
		}

		and:
		def parentConfigurations = [implementation: Mock(Configuration), compileOnly: Mock(Configuration), runtimeOnly: Mock(Configuration)]
		factory.create(_) >> { DependencyBucketIdentifier identifier ->
			def name = identifier.name.get()
			Mock(DependencyBucket) {
				getName() >> name
				getAsConfiguration() >> { parentConfigurations.get(name) }
			}
		}

		and:
		dependencies.create('implementation')
		dependencies.create('compileOnly')

		and:
		childDependencies.create('implementation')
		childDependencies.create('compileOnly')

		when:
		childDependencies.configureEach { childConfig ->
			dependencies.findByName(childConfig.name).ifPresent {
				childConfig.asConfiguration.extendsFrom(it.asConfiguration)
			}
		}
		then:
		1 * childConfigurations.implementation.extendsFrom(parentConfigurations.implementation)
		1 * childConfigurations.compileOnly.extendsFrom(parentConfigurations.compileOnly)

		when:
		dependencies.create('runtimeOnly')
		childDependencies.create('runtimeOnly')
		then:
		1 * childConfigurations.runtimeOnly.extendsFrom(parentConfigurations.runtimeOnly)

		when:
		childDependencies.create('foo')
		then:
		0 * childConfigurations.foo.extendsFrom(_)
	}
}

@Subject(DefaultComponentDependencies)
class DefaultComponentDependenciesGroovyDslTest extends AbstractComponentDependenciesGroovyDslTest {
	def project = ProjectTestUtils.rootProject()
	def factory = new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.configurations), project.dependencies)
	ComponentDependenciesInternal dependenciesUnderTest = project.objects.newInstance(DefaultComponentDependencies, ProjectIdentifier.of('root'), factory)


	def setup() {
		dependenciesUnderTest.create(existingBucketName, ActionUtils.doNothing())
	}
}
