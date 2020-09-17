package dev.nokee.platform.base.internal.tasks

import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.*
import org.gradle.api.Action
import org.gradle.api.Task
import org.gradle.api.tasks.TaskContainer
import org.gradle.api.tasks.TaskProvider
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.platform.base.internal.tasks.TaskName.of as taskName

@Subject(TaskRegistryImpl)
class TaskRegistryTest extends Specification {
	def "can register lifecycle task"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		when:
		subject.register('foo')
		subject.register('bar')
		subject.register('far')

		then:
		1 * taskContainer.register('foo')
		1 * taskContainer.register('bar')
		1 * taskContainer.register('far')
		0 * taskContainer._
	}

	def "can register lifecycle task with action"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Stub(Action)

		when:
		subject.register('foo', action)
		subject.register('bar', action)
		subject.register('far', action)

		then:
		1 * taskContainer.register('foo', action)
		1 * taskContainer.register('bar', action)
		1 * taskContainer.register('far', action)
		0 * taskContainer._
	}

	def "can register typed task"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		when:
		subject.register('foo', TestableTask)
		subject.register('bar', TestableTask)
		subject.register('far', AnotherTestableTask)

		then:
		1 * taskContainer.register('foo', TestableTask)
		1 * taskContainer.register('bar', TestableTask)
		1 * taskContainer.register('far', AnotherTestableTask)
		0 * taskContainer._
	}

	def "can register typed task with action"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Stub(Action)

		when:
		subject.register('foo', TestableTask, action)
		subject.register('bar', TestableTask, action)
		subject.register('far', AnotherTestableTask, action)

		then:
		1 * taskContainer.register('foo', TestableTask, action)
		1 * taskContainer.register('bar', TestableTask, action)
		1 * taskContainer.register('far', AnotherTestableTask, action)
		0 * taskContainer._
	}

	def "can register task via identifier owned by main component"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def owner = ComponentIdentifier.ofMain(TestableComponent, projectIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner))

		then:
		1 * taskContainer.register('foo', TestableTask)
		1 * taskContainer.register('barCpp', TestableTask)
		1 * taskContainer.register('farSwift', AnotherTestableTask)
		0 * taskContainer._
	}

	def "can register task via identifier owned by main component with action"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Stub(Action)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def owner = ComponentIdentifier.ofMain(TestableComponent, projectIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner), action)

		then:
		1 * taskContainer.register('foo', TestableTask, action)
		1 * taskContainer.register('barCpp', TestableTask, action)
		1 * taskContainer.register('farSwift', AnotherTestableTask, action)
		0 * taskContainer._
	}

	def "can register task via identifier owned by non-main component"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def owner = ComponentIdentifier.of(ComponentName.of('test'), TestableComponent, projectIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner))

		then:
		1 * taskContainer.register('fooTest', TestableTask)
		1 * taskContainer.register('barTestCpp', TestableTask)
		1 * taskContainer.register('farTestSwift', AnotherTestableTask)
		0 * taskContainer._
	}

	def "can register task via identifier owned by non-main component with action"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Stub(Action)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def owner = ComponentIdentifier.of(ComponentName.of('test'), TestableComponent, projectIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner), action)

		then:
		1 * taskContainer.register('fooTest', TestableTask, action)
		1 * taskContainer.register('barTestCpp', TestableTask, action)
		1 * taskContainer.register('farTestSwift', AnotherTestableTask, action)
		0 * taskContainer._
	}

	def "can register task via identifier owned by variant of main component"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(TestableComponent, projectIdentifier)
		def owner = VariantIdentifier.of('debug', TestableVariant, componentIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner))

		then:
		1 * taskContainer.register('fooDebug', TestableTask)
		1 * taskContainer.register('barDebugCpp', TestableTask)
		1 * taskContainer.register('farDebugSwift', AnotherTestableTask)
		0 * taskContainer._
	}

	def "can register task via identifier owned by variant of main component with action"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Stub(Action)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(TestableComponent, projectIdentifier)
		def owner = VariantIdentifier.of('debug', TestableVariant, componentIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner), action)

		then:
		1 * taskContainer.register('fooDebug', TestableTask, action)
		1 * taskContainer.register('barDebugCpp', TestableTask, action)
		1 * taskContainer.register('farDebugSwift', AnotherTestableTask, action)
		0 * taskContainer._
	}

	def "can register task via identifier owned by variant of non-main component"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), TestableComponent, projectIdentifier)
		def owner = VariantIdentifier.of('debug', TestableVariant, componentIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner))
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner))

		then:
		1 * taskContainer.register('fooTestDebug', TestableTask)
		1 * taskContainer.register('barTestDebugCpp', TestableTask)
		1 * taskContainer.register('farTestDebugSwift', AnotherTestableTask)
		0 * taskContainer._
	}

	def "can register task via identifier owned by variant of non-main component with action"() {
		given:
		def taskContainer = Mock(TaskContainer)
		def subject = new TaskRegistryImpl(taskContainer)

		and:
		def action = Stub(Action)

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), TestableComponent, projectIdentifier)
		def owner = VariantIdentifier.of('debug', TestableVariant, componentIdentifier)

		when:
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('bar', 'cpp'), TestableTask, owner), action)
		subject.register(TaskIdentifier.of(taskName('far', 'swift'), AnotherTestableTask, owner), action)

		then:
		1 * taskContainer.register('fooTestDebug', TestableTask, action)
		1 * taskContainer.register('barTestDebugCpp', TestableTask, action)
		1 * taskContainer.register('farTestDebugSwift', AnotherTestableTask, action)
		0 * taskContainer._
	}

	def "returns provider from the task container"() {
		given:
		def provider = Stub(TaskProvider)
		def taskContainer = Stub(TaskContainer) {
			register(_) >> provider
			register(_, _ as Action) >> provider
			register(_, _ as Class, _ as Action) >> provider
			register(_, _ as Class) >> provider
		}
		def action = Stub(Action)
		def subject = new TaskRegistryImpl(taskContainer)
		def projectIdentifier = ProjectIdentifier.of('root')

		expect:
		subject.register('foo') == provider
		subject.register('foo', action) == provider
		subject.register('foo', TestableTask) == provider
		subject.register('foo', TestableTask, action) == provider
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, projectIdentifier)) == provider
		subject.register(TaskIdentifier.of(taskName('foo'), TestableTask, projectIdentifier), action) == provider
	}

	interface TestableTask extends Task {}
	interface AnotherTestableTask extends Task {}

	interface TestableComponent extends Component {}
	interface TestableVariant extends Variant {}
}
