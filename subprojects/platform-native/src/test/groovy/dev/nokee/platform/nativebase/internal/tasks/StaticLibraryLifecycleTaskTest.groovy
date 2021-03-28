package dev.nokee.platform.nativebase.internal.tasks

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(StaticLibraryLifecycleTask)
class StaticLibraryLifecycleTaskTest extends Specification {
	def tasks = ProjectTestUtils.rootProject().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', StaticLibraryLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', StaticLibraryLifecycleTask).description == 'Assembles a static library binary containing the main objects.'
	}
}
