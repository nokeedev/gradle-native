package dev.nokee.platform.nativebase.internal.tasks

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(ExecutableLifecycleTask)
class ExecutableLifecycleTaskTest extends Specification {
	def tasks = ProjectTestUtils.rootProject().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', ExecutableLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', ExecutableLifecycleTask).description == 'Assembles a executable binary containing the main objects.'
	}
}
