package dev.nokee.platform.nativebase.internal.tasks

import dev.gradleplugins.grava.testing.util.ProjectTestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(SharedLibraryLifecycleTask)
class SharedLibraryLifecycleTaskTest extends Specification {
	def tasks = ProjectTestUtils.rootProject().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', SharedLibraryLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', SharedLibraryLifecycleTask).description == 'Assembles a shared library binary containing the main objects.'
	}
}
