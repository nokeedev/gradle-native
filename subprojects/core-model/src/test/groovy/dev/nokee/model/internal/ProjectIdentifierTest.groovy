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
package dev.nokee.model.internal

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.util.Path
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.ProjectIdentifier.*

@Subject(ProjectIdentifier)
class ProjectIdentifierTest extends Specification {
	def "can create identifier from string"() {
		expect:
		of('root').name == 'root'
		of('foo').name == 'foo'
		of('bar').name == 'bar'
	}

	def "has display name"() {
		expect:
		of('root').displayName == "project ':'"
		of('foo').displayName == "project ':'"
		of('bar').displayName == "project ':'"
	}

	def "can create identifier for root project without name"() {
		expect:
		def subject = ofRootProject()
		subject.name == null
		subject.path == Path.ROOT
		subject.displayName == "project ':'"
	}

	def "can create child project"() {
		expect:
		def subject = ofChildProject('foo')
		subject.name == 'foo'
		subject.path == Path.ROOT.child('foo')
		subject.displayName == "project ':foo'"
	}

	def "can create nested child project"() {
		expect:
		def subject = ofChildProject('foo', 'bar')
		subject.name == 'bar'
		subject.path == Path.ROOT.child('foo').child('bar')
		subject.displayName == "project ':foo:bar'"
	}

	def "can create identifier from Project instance"() {
		given:
		def project = ProjectTestUtils.rootProject()

		expect:
		of(project).name == project.name
		of(project).displayName == "project '${project.path}'"
	}

	def "can create identifier from child Project instance"() {
		given:
		def rootProject = ProjectTestUtils.rootProject()
		def childProject = ProjectTestUtils.createChildProject(rootProject)

		expect:
		of(childProject).name == childProject.name
		of(childProject).displayName == "project '${childProject.path}'"
	}

	def "has no parent identifier"() {
		given:
		def project = ProjectTestUtils.rootProject()

		expect:
		!of(project).parentIdentifier.present
	}

	def "can compare project identifier instances"() {
		given:
		def foo = ProjectBuilder.builder().withName('foo').build()
		def bar = ProjectBuilder.builder().withName('bar').build()
		def childFoo = ProjectBuilder.builder().withName('foo').withParent(foo).build()

		expect:
		of('foo') == of('foo')
		of('foo') != of('bar')

		and:
		of(foo) == of(foo)
		of(foo) != of(bar)

		and:
		of(foo) != of(childFoo)
	}
}
