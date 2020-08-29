package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.DomainObjectProvider
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.tasks.TaskName.of
import static dev.nokee.utils.DeferredUtils.realize

@Subject(ComponentTasksAdapter)
class ComponentTasksAdapterTest extends Specification {
	def "forwards register to task container"() {
		given:
		def tasks = Mock(TaskContainer)
		def subject = create(tasks)

		when:
		subject.register(of('foo'), DummyTask)

		then:
		1 * tasks.register('foo', DummyTask)
		0 * _
	}

	def "forwards register with action to task container"() {
		given:
		def tasks = Mock(TaskContainer)
		def action = Mock(Action)
		def subject = create(tasks)
		def taskProvider = Mock(TaskProvider)

		when:
		subject.register(of('foo'), DummyTask, action)

		then:
		1 * tasks.register('foo', DummyTask) >> taskProvider
		1 * taskProvider.configure(action)
		0 * _
	}

	def "forwards configure each action to task container"() {
		given:
		def tasks = Mock(TaskContainer)
		def action = Mock(Action)
		def subject = create(tasks)

		when:
		subject.configureEach(action)

		then:
		1 * tasks.configureEach(_)
		0 * _
	}

	def "returns provider when registering a task"() {
		given:
		def tasks = Mock(TaskContainer)
		def subject = create(tasks)

		when:
		def result = subject.register(of('foo'), DummyTask)

		then:
		result instanceof DomainObjectProvider
		result.identifier == identifierOf('foo', DummyTask)
	}

	def "returns provider when registering a task with action"() {
		given:
		def tasks = Mock(TaskContainer) {
			register(_, _) >> Mock(TaskProvider)
		}
		def action = Mock(Action)
		def subject = create(tasks)

		when:
		def result = subject.register(of('foo'), DummyTask, action)

		then:
		result instanceof DomainObjectProvider
		result.identifier == identifierOf('foo', DummyTask)
	}

	def "does not execute configuration actions for unknown task identifier"() {
		given:
		def forwardedAction = null
		def tasks = Mock(TaskContainer) {
			configureEach(_) >> { args -> forwardedAction = args[0] }
		}
		def subject = create(tasks)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		and:
		def task = Mock(Task)

		when:
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'foo'
		0 * action.execute(_)
	}

	def "scopes configuration actions to known task identifier"() {
		given:
		def forwardedAction = null
		def tasks = Mock(TaskContainer) {
			configureEach(_) >> { args -> forwardedAction = args[0] }
		}
		def subject = create(tasks)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		and:
		def task = Mock(Task)

		when:
		subject.register(of('foo'), DummyTask)
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'foo'
		1 * action.execute(task)
	}

	def "scopes configuration actions to known task identifier registered with action"() {
		given:
		def forwardedAction = null
		def tasks = Mock(TaskContainer) {
			configureEach(_) >> { args -> forwardedAction = args[0] }
			register(_, _) >> Mock(TaskProvider)
		}
		def subject = create(tasks)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		and:
		def task = Mock(Task)

		when:
		subject.register(of('foo'), DummyTask, Mock(Action))
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'foo'
		1 * action.execute(task)
	}

	def "can decorate task identifier"() {
		given:
		def tasks = Mock(TaskContainer)
		def identifier = new ComponentIdentifier('test', new ProjectIdentifier('root'))
		def subject = create(tasks, identifier)

		when:
		def result = subject.register(of('foo'), DummyTask)

		then:
		1 * tasks.register('fooTest', DummyTask)
		0 * _

		and:
		result instanceof DomainObjectProvider
		result.identifier == identifierOf('fooTest', DummyTask, identifier)

//		where:
//		parentIdentifier | childIdentifier
//		new ComponentIdentifierEx('test', DummyComponent, new ProjectIdentifier('root'))
	}

	def "can decorate task identifier registered with action"() {
		given:
		def tasks = Mock(TaskContainer)
		def action = Mock(Action)
		def identifier = new ComponentIdentifier('test', new ProjectIdentifier('root'))
		def subject = create(tasks, identifier)
		def taskProvider = Mock(TaskProvider)

		when:
		def result = subject.register(of('foo'), DummyTask, action)

		then:
		1 * tasks.register('fooTest', DummyTask) >> taskProvider
		1 * taskProvider.configure(action)
		0 * _

		and:
		result instanceof DomainObjectProvider
		result.identifier == identifierOf('fooTest', DummyTask, identifier)
	}

	def "does not execute configuration actions for unknown decorated task identifier"() {
		given:
		def forwardedAction = null
		def tasks = Mock(TaskContainer) {
			configureEach(_) >> { args -> forwardedAction = args[0] }
		}
		def subject = create(tasks)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		and:
		def task = Mock(Task)

		when:
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'foo'
		0 * action.execute(_)

		when:
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'fooTest'
		0 * action.execute(_)
	}

	def "scopes configuration actions to known decorated task identifier"() {
		given:
		def forwardedAction = null
		def tasks = Mock(TaskContainer) {
			configureEach(_) >> { args -> forwardedAction = args[0] }
		}
		def identifier = new ComponentIdentifier('test', new ProjectIdentifier('root'))
		def subject = create(tasks, identifier)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		and:
		def task = Mock(Task)

		when:
		subject.register(of('foo'), DummyTask)
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'fooTest'
		1 * action.execute(task)

		when:
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'foo'
		0 * action.execute(_)
	}

	def "scopes configuration actions to known decorated task identifier registered with action"() {
		given:
		def forwardedAction = null
		def tasks = Mock(TaskContainer) {
			configureEach(_) >> { args -> forwardedAction = args[0] }
			register(_, _) >> Mock(TaskProvider)
		}
		def identifier = new ComponentIdentifier('test', new ProjectIdentifier('root'))
		def subject = create(tasks, identifier)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		and:
		def task = Mock(Task)

		when:
		subject.register(of('foo'), DummyTask, Mock(Action))
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'fooTest'
		1 * action.execute(task)

		when:
		forwardedAction.execute(task)
		then:
		1 * task.getName() >> 'foo'
		0 * action.execute(_)
	}


	// Integration test
	def "can configure each task registered"() {
		given:
		def project = ProjectBuilder.builder().build()
		def subject = create(project.tasks, ProjectIdentifier.of(project))

		and:
		subject.register(of('foo'), DummyTask)
		subject.register(of('bar'), DummyTask, Mock(Action))

		when:
		def configuredTasks = []
		subject.configureEach { configuredTasks << it.name }

		then:
		realize(project.tasks)
		configuredTasks == ['foo', 'bar']
	}

	def "can configure each decorated task registered"() {
		given:
		def project = ProjectBuilder.builder().build()
		def identifier = new ComponentIdentifier('test', ProjectIdentifier.of(project))
		def subject = create(project.tasks, identifier)

		and:
		subject.register(of('foo'), DummyTask)
		subject.register(of('bar'), DummyTask, Mock(Action))

		when:
		def configuredTasks = []
		subject.configureEach { configuredTasks << it.name }

		then:
		realize(project.tasks)
		configuredTasks == ['fooTest', 'barTest']
	}

	def "can configure each task after the configuration action is registered"() {
		given:
		def project = ProjectBuilder.builder().build()
		def subject = create(project.tasks, ProjectIdentifier.of(project))

		and:
		subject.register(of('foo'), DummyTask)
		subject.register(of('bar'), DummyTask, Mock(Action))

		and:
		def configuredTasks = []
		subject.configureEach { configuredTasks << it.name }

		when:
		subject.register(of('far'), DummyTask)

		then:
		realize(project.tasks)
		configuredTasks == ['foo', 'bar', 'far']
	}

	def "can configure each task eagerly registered"() {
		given:
		def project = ProjectBuilder.builder().build()
		def subject = create(project.tasks, ProjectIdentifier.of(project))

		and:
		def configuredTasks = []
		subject.configureEach { configuredTasks << it.name }
		project.tasks.all {} // force realization

		when:
		subject.register(of('foo'), DummyTask)
		subject.register(of('bar'), DummyTask, Mock(Action))

		then:
		configuredTasks == ['foo', 'bar']
	}

	// TODO: Test laziness

	// TODO: Unit test when known task identifier registry has other entries not under tasks component identifier

	def <T extends Task> TaskIdentifier<T> identifierOf(String name, Class<T> type, DomainObjectIdentifierInternal owner = new ProjectIdentifier('root')) {
		return new TaskIdentifier<>(name, type, owner)
	}

	ComponentTasksAdapter create(TaskContainer tasks, DomainObjectIdentifierInternal identifier = new ProjectIdentifier('root')) {
		return new ComponentTasksAdapter(identifier, tasks)
	}

	static class DummyTask extends DefaultTask {}
}
