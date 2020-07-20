package dev.nokee.utils

import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.TaskUtils.temporaryDirectoryPath

@Subject(TaskUtils)
class TaskUtilsTest extends Specification {
	def "can get task temporary directory path"() {
		def task = Mock(Task)
		task.getName() >> 'foo'

		expect:
		temporaryDirectoryPath(task) == 'tmp/foo'
	}
}
