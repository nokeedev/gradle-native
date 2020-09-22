package dev.nokee.platform.nativebase.internal.tasks

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectsLifecycleTask)
class ObjectsLifecycleTaskTest extends Specification {
	def tasks = ProjectBuilder.builder().build().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', ObjectsLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', ObjectsLifecycleTask).description == 'Assembles main objects.'
	}
}
