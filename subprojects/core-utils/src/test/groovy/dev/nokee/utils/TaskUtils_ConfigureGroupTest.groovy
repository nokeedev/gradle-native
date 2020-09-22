package dev.nokee.utils

import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TaskUtils.configureGroup

@Subject(TaskUtils)
class TaskUtils_ConfigureGroupTest extends Specification {
	def "can configure group"() {
		given:
		def task = Mock(Task)

		when:
		configureGroup('build').execute(task)

		then:
		1 * task.setGroup('build')
		0 * task._
	}

	def "throws exception when group is null"() {
		when:
		configureGroup(null)

		then:
		thrown(NullPointerException)
	}

	def "actions are equals for the same dependency path instance"() {
		expect:
		configureGroup('build') == configureGroup('build')
		configureGroup('build') != configureGroup('verification')
	}

	def "action toString() explains where the action comes from"() {
		expect:
		configureGroup('build').toString() == 'TaskUtils.configureGroup(build)'
		configureGroup('verification').toString() == 'TaskUtils.configureGroup(verification)'
	}
}
