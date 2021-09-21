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
package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import spock.lang.Specification
import spock.lang.Subject

@Subject(DefaultDependencyBucket)
class DefaultDependencyBucketTest extends Specification {
	def dependency = Mock(ModuleDependency)
	def dependencyHandler = Mock(DependencyHandler)
	def dependencySet = Mock(DependencySet)
	def configuration = Mock(Configuration) {
		getDependencies() >> dependencySet
	}
	def subject = new DefaultDependencyBucket('foo', configuration, dependencyHandler)

	def "can add dependency"() {
		given:
		def notation = new Object()

		when:
		subject.addDependency(notation)
		then:
		1 * dependencyHandler.create(notation) >> dependency
		1 * dependencySet.add(dependency)

		when:
		subject.addDependency(notation, Mock(Action))
		then:
		1 * dependencyHandler.create(notation) >> dependency
		1 * dependencySet.add(dependency)
	}

	def "can configure dependency"() {
		given:
		def notation = new Object()
		def action = Mock(Action)

		and:
		dependencyHandler.create(_) >> dependency

		when:
		subject.addDependency(notation, action)

		then:
		1 * action.execute(dependency)
	}

	def "has name unrelated to configuration"() {
		when:
		def name = subject.name

		then:
		name == 'foo'

		and:
		0 * _ // name comes from bucket only
	}
}
