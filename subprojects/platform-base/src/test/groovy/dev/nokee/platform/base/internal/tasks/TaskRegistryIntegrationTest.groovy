package dev.nokee.platform.base.internal.tasks

import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskRegistryImpl)
abstract class AbstractTaskRegistryIntegrationTest extends Specification {
	def "can maybe register lifecycle task"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		assert !taskContainer.names.contains('foo')

		when:
		def result1 = registerIfAbsent(subject)
		then:
		taskContainer.named('foo') == result1
		result1 != null

		when:
		def result2 = registerIfAbsent(subject)
		then:
		result2 == result1
	}

	def "throws exception when maybe registering lifecycle task because of type mismatch"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		taskContainer.register('foo', WrongTask)

		when:
		registerIfAbsent(subject)
		then:
		def ex = thrown(InvalidUserDataException)
		ex.message == "Could not register task 'foo': Task type requested (${expectedTaskType}) does not match actual type (${WrongTask.canonicalName})."
	}

	def "does not resolve task when checking if task is present"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		taskContainer.configureEach { throw new RuntimeException() }

		when:
		def result1 = registerIfAbsent(subject)
		def result2 = registerIfAbsent(subject)

		then:
		result2 == result1
	}

	def "can maybe register lifecycle task with action"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		assert !taskContainer.names.contains('foo')

		and:
		def action = Mock(Action)

		when:
		def result1 = registerIfAbsent(subject, action)
		then:
		taskContainer.named('foo') == result1
		result1 != null

		when:
		def result2 = registerIfAbsent(subject, action)
		then:
		result2 == result1
	}

	def "throws exception when maybe registering lifecycle task with action because of type mismatch"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Mock(Action)

		and:
		taskContainer.create('foo', WrongTask) {}

		when:
		registerIfAbsent(subject, action)
		then:
		def ex = thrown(InvalidUserDataException)
		ex.message == "Could not register task 'foo': Task type requested (${expectedTaskType}) does not match actual type (${WrongTask.canonicalName})."
		0 * action.execute(_)
	}

	def "does not resolve task when checking if task is present with action"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		taskContainer.configureEach { throw new RuntimeException() }

		when:
		def result1 = registerIfAbsent(subject, Stub(Action))
		def result2 = registerIfAbsent(subject, Stub(Action))

		then:
		result2 == result1
	}

	def "does not execute action when already registered"() {
		given:
		def taskContainer = ProjectBuilder.builder().build().tasks
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		taskContainer.all {} // force realize all tasks upon creation

		and:
		def action = Mock(Action)

		when:
		registerIfAbsent(subject, action)
		then:
		1 * action.execute(_)

		when:
		registerIfAbsent(subject, action)
		then:
		0 * action.execute(_)
	}

	protected abstract TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject)
	protected abstract TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject, Action<? super Task> action)
	protected abstract String getExpectedTaskType()

	static class WrongTask extends DefaultTask {}
}

class TaskRegistry_RegisterIfAbsentLifecycleIntegrationTest extends AbstractTaskRegistryIntegrationTest {
	protected TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject) {
		return subject.registerIfAbsent('foo')
	}

	protected TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject, Action<? super Task> action) {
		return subject.registerIfAbsent('foo', action)
	}

	@Override
	protected String getExpectedTaskType() {
		return Task.canonicalName
	}
}

class TaskRegistry_RegisterIfAbsentTypeIntegrationTest extends AbstractTaskRegistryIntegrationTest {
	protected TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject) {
		return subject.registerIfAbsent('foo', TestableTask)
	}

	protected TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject, Action<? super Task> action) {
		return subject.registerIfAbsent('foo', TestableTask, action)
	}

	@Override
	protected String getExpectedTaskType() {
		return TestableTask.canonicalName
	}

	static class TestableTask extends DefaultTask {}
}

class TaskRegistry_RegisterIfAbsentTaskIdentifierIntegrationTest extends AbstractTaskRegistryIntegrationTest {
	protected TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject) {
		return subject.registerIfAbsent(TaskIdentifier.of(TaskName.of('foo'), TestableTask, ProjectIdentifier.of('root')))
	}

	protected TaskProvider<? extends Task> registerIfAbsent(TaskRegistry subject, Action<? super Task> action) {
		return subject.registerIfAbsent(TaskIdentifier.of(TaskName.of('foo'), TestableTask, ProjectIdentifier.of('root')), action)
	}

	@Override
	protected String getExpectedTaskType() {
		return TestableTask.canonicalName
	}

	static class TestableTask extends DefaultTask {}
}
