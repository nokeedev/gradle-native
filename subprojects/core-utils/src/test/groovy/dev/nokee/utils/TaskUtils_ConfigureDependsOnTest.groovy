package dev.nokee.utils


import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TaskUtils.configureDependsOn

@Subject(TaskUtils)
class TaskUtils_ConfigureDependsOnTest extends Specification {
	def "can configure a single path"() {
		given:
		def task = Mock(Task)

		when:
		configureDependsOn(':foo').execute(task)

		then:
		1 * task.dependsOn(':foo')
		0 * task._
	}

	def "can configure a multiple paths"() {
		given:
		def task = Mock(Task)

		when:
		configureDependsOn(':foo', ':bar').execute(task)

		then:
		1 * task.dependsOn(':foo', ':bar')
		0 * task._
	}

	def "throws exception when path is null"() {
		when:
		configureDependsOn(null)
		then:
		thrown(NullPointerException)

		when:
		configureDependsOn(':foo', null)
		then:
		thrown(NullPointerException)

		when:
		configureDependsOn(':foo', ':bar', null)
		then:
		thrown(NullPointerException)
	}

	def "actions are equals for the same dependency path instance"() {
		expect:
		configureDependsOn(':some:path') == configureDependsOn(':some:path')
		configureDependsOn(':some:path') != configureDependsOn(':foo')
	}

	def "action toString() explains where the action comes from"() {
		expect:
		configureDependsOn(':some:path').toString() == 'TaskUtils.configureDependsOn([:some:path])'
		configureDependsOn(':some:path', ':some:other-path').toString() == 'TaskUtils.configureDependsOn([:some:path, :some:other-path])'
	}
}
