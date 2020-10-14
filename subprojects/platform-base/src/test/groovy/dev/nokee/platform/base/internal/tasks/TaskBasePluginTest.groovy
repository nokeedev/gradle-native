package dev.nokee.platform.base.internal.tasks

import dev.nokee.platform.base.internal.plugins.TaskBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskBasePlugin)
class TaskBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers task registry service"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.extensions.findByType(TaskRegistry) != null
	}

	def "registers task configurer service"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.extensions.findByType(TaskConfigurer) != null
	}

	def "registers task repository service"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.extensions.findByType(TaskRepository) != null
	}

	def "registers task view factory"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.extensions.findByType(TaskViewFactory) != null
	}

	def "registers known task factory"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.extensions.findByType(KnownTaskFactory) != null
	}
}
