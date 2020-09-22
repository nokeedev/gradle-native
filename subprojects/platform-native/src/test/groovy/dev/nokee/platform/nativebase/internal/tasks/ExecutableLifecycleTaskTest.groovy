package dev.nokee.platform.nativebase.internal.tasks

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ExecutableLifecycleTask)
class ExecutableLifecycleTaskTest extends Specification {
	def tasks = ProjectBuilder.builder().build().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', ExecutableLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', ExecutableLifecycleTask).description == 'Assembles a executable binary containing the main objects.'
	}
}
