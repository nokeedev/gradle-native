package dev.nokee.platform.base.internal.tasks

import dev.nokee.platform.base.internal.plugins.TaskBasePlugin
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class TaskBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers task registry service"() {
		when:
		project.apply plugin: TaskBasePlugin

		then:
		project.extensions.findByType(TaskRegistry) != null
	}
}
