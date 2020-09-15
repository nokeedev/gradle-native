package dev.nokee.platform.base.internal

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.ProjectIdentifier.of

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
		of('root').displayName == "project ':root'"
		of('foo').displayName == "project ':foo'"
		of('bar').displayName == "project ':bar'"
	}

	def "can create identifier from Project instance"() {
		given:
		def project = ProjectBuilder.builder().build()

		expect:
		of(project).name == project.name
		of(project).displayName == "project ':${project.name}'"
	}

	def "can create identifier from child Project instance"() {
		given:
		def rootProject = ProjectBuilder.builder().build()
		def childProject = ProjectBuilder.builder().withParent(rootProject).build()

		expect:
		of(childProject).name == childProject.name
		of(childProject).displayName == "project ':${rootProject.name}:${childProject.name}'"
	}

	def "has no parent identifier"() {
		given:
		def project = ProjectBuilder.builder().build()

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
