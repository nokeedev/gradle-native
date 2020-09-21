package dev.nokee.platform.nativebase.internal.tasks

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(SharedLibraryLifecycleTask)
class SharedLibraryLifecycleTaskTest extends Specification {
	def tasks = ProjectBuilder.builder().build().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', SharedLibraryLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', SharedLibraryLifecycleTask).description == 'Assembles a shared library binary containing the main objects.'
	}
}
