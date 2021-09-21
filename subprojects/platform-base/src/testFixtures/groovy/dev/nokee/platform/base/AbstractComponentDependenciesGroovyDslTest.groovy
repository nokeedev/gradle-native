/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base

import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies
import org.gradle.api.artifacts.ModuleDependency
import spock.lang.Specification

abstract class AbstractComponentDependenciesGroovyDslTest extends Specification {
	protected abstract ComponentDependenciesInternal getDependenciesUnderTest()

	protected String getExistingBucketName() {
		return 'foo'
	}

	protected String getMissingBucketName() {
		return 'missing'
	}

	protected abstract void assertHasDependency(DependencyBucket bucket, String dependency)

	def "can access existing bucket as property"() {
		expect:
		dependenciesUnderTest."${existingBucketName}" == dependenciesUnderTest.getByName(existingBucketName)
	}

	def "can add dependency as method call"() {
		when:
		dependenciesUnderTest."${existingBucketName}"('com.example:foo:4.2')

		then:
		assertHasDependency(dependenciesUnderTest.getByName(existingBucketName), 'com.example:foo:4.2')
	}

	def "can include configuration when adding dependency as method call"() {
		when:
		def configuredDependencies = []
		dependenciesUnderTest."${existingBucketName}"('com.example:foo:4.2') { ModuleDependency dependency ->
			configuredDependencies << "${dependency.group}:${dependency.name}:${dependency.version}"
		}

		then:
		configuredDependencies == ['com.example:foo:4.2']
	}

	def "throws exceptions when adding dependency as method call with configuration action to missing bucket"() {
		when:
		dependenciesUnderTest."${missingBucketName}"('com.example:foo:4.2') {
			throw new AssertionError()
		}

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Dependency bucket named '${missingBucketName}' couldn't be found."
	}

	def "throws exceptions when adding dependency as method call to missing bucket"() {
		when:
		dependenciesUnderTest."${missingBucketName}"('com.example:foo:4.2')

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Dependency bucket named '${missingBucketName}' couldn't be found."
	}

	def "throws exceptions when accessing missing bucket as property"() {
		when:
		dependenciesUnderTest."${missingBucketName}"

		then:
		def ex = thrown(MissingPropertyException)
		ex.message == "Could not get unknown property '${missingBucketName}' for object of type ${DefaultComponentDependencies.canonicalName}."
	}
}
