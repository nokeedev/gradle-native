package dev.nokee.model.internal

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
		def project = ProjectBuilder.builder().build()

		expect:
		of(project).name == project.name
		of(project).displayName == "project '${project.path}'"
	}

	def "can create identifier from child Project instance"() {
		given:
		def rootProject = ProjectBuilder.builder().build()
		def childProject = ProjectBuilder.builder().withParent(rootProject).build()

		expect:
		of(childProject).name == childProject.name
		of(childProject).displayName == "project '${childProject.path}'"
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
