package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.DomainObjectIdentifier
import dev.nokee.model.internal.*
import dev.nokee.platform.base.Component
import dev.nokee.platform.base.Variant
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ComponentName
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.InvalidUserDataException
import org.gradle.api.Task
import org.gradle.api.tasks.TaskProvider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(TaskRegistryImpl)
class TaskRegistryTest extends Specification {
	protected eventPublisher = new DomainObjectEventPublisherImpl()
	protected taskContainer = ProjectBuilder.builder().build().getTasks()

	protected TaskRegistry newSubject() {
		return new TaskRegistryImpl(DEFAULT_OWNER_IDENTIFIER, eventPublisher, taskContainer)
	}

	protected <U extends DomainObjectEvent> DomainObjectEventSubscriber<U> subscribed(Class<U> eventType) {
		def result = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> eventType
		}
		eventPublisher.subscribe(result)
		return result
	}

	protected static <U extends Task> TaskIdentifier<U> identifierOf(String name, Class<U> type) {
		return TaskIdentifier.of(TaskName.of(name), type, DEFAULT_OWNER_IDENTIFIER)
	}

	protected static <U extends Task> TaskIdentifier<U> identifierOf(String name, Class<U> type, DomainObjectIdentifier owner) {
		return TaskIdentifier.of(TaskName.of(name), type, owner)
	}

	@Unroll
	def "can register task"(register) {
		given:
		def subject = newSubject()

		when:
		register(subject, 'foo')
		register(subject, 'bar')
		register(subject, 'far')

		then:
		taskContainer.foo != null
		taskContainer.bar != null
		taskContainer.far != null

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "differs task creation until tasks are realized"(register) {
		given:
		def subject = newSubject()

		and:
		def action = Mock(Action)
		taskContainer.configureEach(action)

		when:
		def fooProvider = register(subject, 'foo')
		def barProvider = register(subject, 'bar')
		def farProvider = register(subject, 'far')
		then:
		0 * action.execute(_)

		when:
		fooProvider.get()
		then:
		1 * action.execute({ it.name == 'foo' })

		when:
		barProvider.get()
		then:
		1 * action.execute({ it.name == 'bar' })

		when:
		farProvider.get()
		then:
		1 * action.execute({ it.name == 'far' })

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "can register task with action"(register) {
		given:
		def subject = newSubject()

		and:
		def action = Stub(Action)

		when:
		register(subject, 'foo', action)
		register(subject, 'bar', action)
		register(subject, 'far', action)

		then:
		taskContainer.foo != null
		taskContainer.bar != null
		taskContainer.far != null

		where:
		register << REGISTER_FUNCTIONS_WITH_ACTION_UNDER_TEST
	}

	@Unroll
	def "executes configuration action only when tasks are realized"(register) {
		given:
		def subject = newSubject()

		and:
		def action = Mock(Action)

		when:
		register(subject, 'foo', action)
		register(subject, 'bar', action)
		register(subject, 'far', action)
		then:
		0 * action.execute(_)

		when:
		taskContainer.iterator().next() // force realize all tasks
		then:
		1 * action.execute({ it.name == 'foo' })
		1 * action.execute({ it.name == 'bar' })
		1 * action.execute({ it.name == 'far' })
		0 * action.execute(_)

		where:
		register << REGISTER_FUNCTIONS_WITH_ACTION_UNDER_TEST
	}

	@Unroll
	def "can register task with type"(register) {
		given:
		def subject = newSubject()

		when:
		register(subject, 'foo', TestableTask)
		register(subject, 'bar', TestableTask)
		register(subject, 'far', AnotherTestableTask)

		then:
		taskContainer.foo instanceof TestableTask
		taskContainer.bar instanceof TestableTask
		taskContainer.far instanceof AnotherTestableTask

		where:
		register << REGISTER_FUNCTIONS_WITH_TYPE_UNDER_TEST
	}

	@Unroll
	def "can register task owned by main component"(register) {
		given:
		def subject = newSubject()

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def owner = ComponentIdentifier.ofMain(TestableComponent, projectIdentifier)

		when:
		register(subject, 'foo', TestableTask, owner)
		register(subject, 'bar', TestableTask, owner)
		register(subject, 'far', AnotherTestableTask, owner)

		then:
		taskContainer.foo instanceof TestableTask
		taskContainer.bar instanceof TestableTask
		taskContainer.far instanceof AnotherTestableTask

		where:
		register << REGISTER_FUNCTIONS_WITH_OWNER_UNDER_TEST
	}

	@Unroll
	def "can register task owned by non-main component"(register) {
		given:
		def subject = newSubject()

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def owner = ComponentIdentifier.of(ComponentName.of('test'), TestableComponent, projectIdentifier)

		when:
		register(subject, 'foo', TestableTask, owner)
		register(subject, 'bar', TestableTask, owner)
		register(subject, 'far', AnotherTestableTask, owner)

		then:
		taskContainer.fooTest instanceof TestableTask
		taskContainer.barTest instanceof TestableTask
		taskContainer.farTest instanceof AnotherTestableTask

		where:
		register << REGISTER_FUNCTIONS_WITH_OWNER_UNDER_TEST
	}

	@Unroll
	def "can register task owned by variant of main component"(register) {
		given:
		def subject = newSubject()

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.ofMain(TestableComponent, projectIdentifier)
		def owner = VariantIdentifier.of('debug', TestableVariant, componentIdentifier)

		when:
		register(subject, 'foo', TestableTask, owner)
		register(subject, 'bar', TestableTask, owner)
		register(subject, 'far', AnotherTestableTask, owner)

		then:
		taskContainer.fooDebug instanceof TestableTask
		taskContainer.barDebug instanceof TestableTask
		taskContainer.farDebug instanceof AnotherTestableTask

		where:
		register << REGISTER_FUNCTIONS_WITH_OWNER_UNDER_TEST
	}

	@Unroll
	def "can register task owned by variant of non-main component"(register) {
		given:
		def subject = newSubject()

		and:
		def projectIdentifier = ProjectIdentifier.of('root')
		def componentIdentifier = ComponentIdentifier.of(ComponentName.of('test'), TestableComponent, projectIdentifier)
		def owner = VariantIdentifier.of('debug', TestableVariant, componentIdentifier)

		when:
		register(subject, 'foo', TestableTask, owner)
		register(subject, 'bar', TestableTask, owner)
		register(subject, 'far', AnotherTestableTask, owner)

		then:
		taskContainer.fooTestDebug instanceof TestableTask
		taskContainer.barTestDebug instanceof TestableTask
		taskContainer.farTestDebug instanceof AnotherTestableTask

		where:
		register << REGISTER_FUNCTIONS_WITH_OWNER_UNDER_TEST
	}

	@Unroll
	def "returns provider from the task container"(register) {
		given:
		def subject = newSubject()

		expect:
		def provider = register(subject, 'foo')
		provider instanceof TaskProvider
		provider.name == 'foo'
		provider.get() == taskContainer.foo

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "publishes discovered entity event when registering lifecycle task"(register) {
		given:
		def subject = newSubject()

		and:
		def subscriber = subscribed(DomainObjectDiscovered)

		when:
		register(subject, 'foo')
		register(subject, 'bar')
		register(subject, 'far')

		then:
		1 * subscriber.handle(new DomainObjectDiscovered<>(register.identifierOf('foo')))
		1 * subscriber.handle(new DomainObjectDiscovered<>(register.identifierOf('bar')))
		1 * subscriber.handle(new DomainObjectDiscovered<>(register.identifierOf('far')))
		0 * subscriber.handle(_)

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "publishes discovered realizable entity event when registering lifecycle task"(register) {
		given:
		def subject = newSubject()

		and:
		def subscriber = subscribed(RealizableDomainObjectDiscovered)

		when:
		register(subject, 'foo')
		register(subject, 'bar')
		register(subject, 'far')

		then:
		1 * subscriber.handle({ it == discoveredEvent(register, 'foo') })
		1 * subscriber.handle({ it == discoveredEvent(register, 'bar') })
		1 * subscriber.handle({ it == discoveredEvent(register, 'far') })
		0 * subscriber.handle(_)

		where:
		register << REGISTER_FUNCTIONS_UNDER_TEST
	}

	private RealizableDomainObjectDiscovered discoveredEvent(def register, String name) {
		return new RealizableDomainObjectDiscovered(register.identifierOf(name), new RealizableGradleProvider(register.identifierOf(name), taskContainer.named(name), eventPublisher))
	}

	@Unroll
	def "throws exception when type mismatch during maybe registering tasks"(register) {
		given:
		def subject = newSubject()

		and:
		taskContainer.register('foo', WrongTask)

		when:
		register(subject, 'foo')
		then:
		def ex = thrown(InvalidUserDataException)
		ex.message == "Could not register task 'foo': Task type requested (${register.defaultTaskType.canonicalName}) does not match actual type (${WrongTask.canonicalName})."

		where:
		register << REGISTER_IF_ABSENT_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "does not resolve task when checking if task is present"(register) {
		given:
		def subject = newSubject()

		and:
		taskContainer.configureEach { throw new RuntimeException() }

		when:
		def result1 = register(subject, 'foo')
		def result2 = register(subject, 'foo')

		then:
		result2 == result1

		where:
		register << REGISTER_IF_ABSENT_FUNCTIONS_UNDER_TEST
	}

	@Unroll
	def "does not execute action when already registered"(register) {
		given:
		def subject = newSubject()

		and:
		taskContainer.all {} // force realize all tasks upon creation

		and:
		def action = Mock(Action)

		when:
		register(subject, 'foo', action)
		then:
		1 * action.execute(_)

		when:
		register(subject, 'foo', action)
		then:
		0 * action.execute(_)

		where:
		register << REGISTER_IF_ABSENT_FUNCTIONS_WITH_ACTION_UNDER_TEST
	}

	//region register method fixtures
	private static final DomainObjectIdentifier DEFAULT_OWNER_IDENTIFIER = ProjectIdentifier.of('root')
	static <T extends Task> Action<T> doNothing() {
		return new Action<T>() {
			void execute(T o) {
				// do nothing...
				// Note: we don't use ActionUtils.doNothing() as some code tries to optimize in the presence of obvious "do nothing" action.
			}
		}
	}

	interface RegisterFunction {
		TaskProvider<Task> call(TaskRegistry subject, String name)

		TaskIdentifier identifierOf(String name)
	}

	interface RegisterWithActionFunction {
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action)

		TaskIdentifier identifierOf(String name)
	}

	interface TypeAwareRegisterMethod {
		public <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type)
	}

	interface OwnerAwareRegisterMethod {
		public <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type, DomainObjectIdentifier owner)
	}

	interface IfAbsentRegisterMethod {
		TaskProvider<Task> call(TaskRegistry subject, String name);

		Class<? extends Task> getDefaultTaskType()
	}

	interface ActionAwareIfAbsentRegisterMethod {
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action);
	}

	enum StringRegisterFunction implements RegisterFunction {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.register(name)
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, Task)
		}
	}

	enum StringActionRegisterFunction implements RegisterFunction, RegisterWithActionFunction {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.register(name, TaskRegistryTest.doNothing())
		}

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action) {
			subject.register(name, action)
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, Task)
		}
	}

	enum StringIfAbsentRegisterFunction implements RegisterFunction, IfAbsentRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.registerIfAbsent(name)
		}

		@Override
		Class<? extends Task> getDefaultTaskType() {
			return Task
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, Task)
		}
	}

	enum StringActionIfAbsentRegisterFunction implements RegisterFunction, RegisterWithActionFunction, IfAbsentRegisterMethod, ActionAwareIfAbsentRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.registerIfAbsent(name, TaskRegistryTest.doNothing())
		}

		@Override
		Class<? extends Task> getDefaultTaskType() {
			return Task
		}

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action) {
			subject.registerIfAbsent(name, action)
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, Task)
		}
	}

	enum StringTypeRegisterFunction implements RegisterFunction, TypeAwareRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.register(name, TaskRegistryTest.TestableTask)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.register(name, type)
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, TaskRegistryTest.TestableTask)
		}
	}

	enum StringTypeActionRegisterFunction implements RegisterFunction, RegisterWithActionFunction, TypeAwareRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.register(name, TaskRegistryTest.TestableTask, TaskRegistryTest.doNothing())
		}

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action) {
			subject.register(name, TaskRegistryTest.TestableTask, action)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.register(name, type, TaskRegistryTest.doNothing())
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, TaskRegistryTest.TestableTask)
		}
	}

	enum StringTypeIfAbsentRegisterFunction implements RegisterFunction, TypeAwareRegisterMethod, IfAbsentRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.registerIfAbsent(name, TaskRegistryTest.TestableTask)
		}

		@Override
		Class<? extends Task> getDefaultTaskType() {
			return TaskRegistryTest.TestableTask
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.registerIfAbsent(name, type)
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, TaskRegistryTest.TestableTask)
		}
	}

	enum StringTypeActionIfAbsentRegisterFunction implements RegisterFunction, RegisterWithActionFunction, TypeAwareRegisterMethod, IfAbsentRegisterMethod, ActionAwareIfAbsentRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.registerIfAbsent(name, TaskRegistryTest.TestableTask, TaskRegistryTest.doNothing())
		}

		@Override
		Class<? extends Task> getDefaultTaskType() {
			return TaskRegistryTest.TestableTask
		}

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action) {
			subject.registerIfAbsent(name, TaskRegistryTest.TestableTask, action)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.registerIfAbsent(name, type, TaskRegistryTest.doNothing())
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, TaskRegistryTest.TestableTask)
		}
	}

	enum TaskIdentifierRegisterFunction implements RegisterFunction, TypeAwareRegisterMethod, OwnerAwareRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.register(identifierOf(name))
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.register(TaskRegistryTest.identifierOf(name, type))
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskRegistryTest.identifierOf(name, TaskRegistryTest.TestableTask)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type, DomainObjectIdentifier owner) {
			subject.register(TaskRegistryTest.identifierOf(name, type, owner))
		}
	}

	enum TaskIdentifierActionRegisterFunction implements RegisterFunction, RegisterWithActionFunction, TypeAwareRegisterMethod, OwnerAwareRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.register(identifierOf(name), TaskRegistryTest.doNothing())
		}

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action) {
			subject.register(identifierOf(name), action)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.register(TaskRegistryTest.identifierOf(name, type), TaskRegistryTest.doNothing())
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskIdentifier.of(TaskName.of(name), TaskRegistryTest.TestableTask, DEFAULT_OWNER_IDENTIFIER)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type, DomainObjectIdentifier owner) {
			subject.register(TaskRegistryTest.identifierOf(name, type, owner), TaskRegistryTest.doNothing())
		}
	}

	enum TaskIdentifierIfAbsentRegisterFunction implements RegisterFunction, TypeAwareRegisterMethod, OwnerAwareRegisterMethod, IfAbsentRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.registerIfAbsent(identifierOf(name))
		}

		@Override
		Class<? extends Task> getDefaultTaskType() {
			return TaskRegistryTest.TestableTask
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.registerIfAbsent(TaskRegistryTest.identifierOf(name, type))
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskIdentifier.of(TaskName.of(name), TaskRegistryTest.TestableTask, DEFAULT_OWNER_IDENTIFIER)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type, DomainObjectIdentifier owner) {
			subject.registerIfAbsent(TaskRegistryTest.identifierOf(name, type, owner))
		}
	}

	enum TaskIdentifierActionIfAbsentRegisterFunction implements RegisterFunction, RegisterWithActionFunction, TypeAwareRegisterMethod, OwnerAwareRegisterMethod, IfAbsentRegisterMethod, ActionAwareIfAbsentRegisterMethod {
		INSTANCE;

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name) {
			subject.registerIfAbsent(identifierOf(name), TaskRegistryTest.doNothing())
		}

		@Override
		Class<? extends Task> getDefaultTaskType() {
			return TaskRegistryTest.TestableTask
		}

		@Override
		TaskProvider<Task> call(TaskRegistry subject, String name, Action<? extends Task> action) {
			subject.registerIfAbsent(identifierOf(name), action)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type) {
			subject.registerIfAbsent(TaskRegistryTest.identifierOf(name, type), TaskRegistryTest.doNothing())
		}

		@Override
		TaskIdentifier identifierOf(String name) {
			return TaskIdentifier.of(TaskName.of(name), TaskRegistryTest.TestableTask, DEFAULT_OWNER_IDENTIFIER)
		}

		@Override
		def <T extends Task> TaskProvider<Task> call(TaskRegistry subject, String name, Class<T> type, DomainObjectIdentifier owner) {
			subject.registerIfAbsent(TaskRegistryTest.identifierOf(name, type, owner), TaskRegistryTest.doNothing())
		}
	}

	private static final List<RegisterFunction> REGISTER_FUNCTIONS_UNDER_TEST = [StringRegisterFunction.INSTANCE, StringActionRegisterFunction.INSTANCE, StringTypeRegisterFunction.INSTANCE, StringTypeActionRegisterFunction.INSTANCE, TaskIdentifierRegisterFunction.INSTANCE, TaskIdentifierActionRegisterFunction.INSTANCE, StringIfAbsentRegisterFunction.INSTANCE, StringActionIfAbsentRegisterFunction.INSTANCE, StringTypeIfAbsentRegisterFunction.INSTANCE, StringTypeActionIfAbsentRegisterFunction.INSTANCE, TaskIdentifierIfAbsentRegisterFunction.INSTANCE, TaskIdentifierActionIfAbsentRegisterFunction.INSTANCE]

	private static final List<RegisterWithActionFunction> REGISTER_FUNCTIONS_WITH_ACTION_UNDER_TEST = [StringActionRegisterFunction.INSTANCE, StringTypeActionRegisterFunction.INSTANCE, TaskIdentifierActionRegisterFunction.INSTANCE, StringActionIfAbsentRegisterFunction.INSTANCE, StringTypeActionIfAbsentRegisterFunction.INSTANCE, TaskIdentifierActionIfAbsentRegisterFunction.INSTANCE]

	private static final List<TypeAwareRegisterMethod> REGISTER_FUNCTIONS_WITH_TYPE_UNDER_TEST = [StringTypeRegisterFunction.INSTANCE, StringTypeActionRegisterFunction.INSTANCE, TaskIdentifierRegisterFunction.INSTANCE, TaskIdentifierActionRegisterFunction.INSTANCE, StringTypeIfAbsentRegisterFunction.INSTANCE, StringTypeActionIfAbsentRegisterFunction.INSTANCE, TaskIdentifierIfAbsentRegisterFunction.INSTANCE, TaskIdentifierActionIfAbsentRegisterFunction.INSTANCE]

	private static final List<OwnerAwareRegisterMethod> REGISTER_FUNCTIONS_WITH_OWNER_UNDER_TEST = [TaskIdentifierRegisterFunction.INSTANCE, TaskIdentifierActionRegisterFunction.INSTANCE, TaskIdentifierIfAbsentRegisterFunction.INSTANCE, TaskIdentifierActionIfAbsentRegisterFunction.INSTANCE]

	private static final List<IfAbsentRegisterMethod> REGISTER_IF_ABSENT_FUNCTIONS_UNDER_TEST = [StringIfAbsentRegisterFunction.INSTANCE, StringActionIfAbsentRegisterFunction.INSTANCE, StringTypeIfAbsentRegisterFunction.INSTANCE, StringTypeActionIfAbsentRegisterFunction.INSTANCE, TaskIdentifierIfAbsentRegisterFunction.INSTANCE, TaskIdentifierActionIfAbsentRegisterFunction.INSTANCE]

	private static final List<ActionAwareIfAbsentRegisterMethod> REGISTER_IF_ABSENT_FUNCTIONS_WITH_ACTION_UNDER_TEST = [StringActionIfAbsentRegisterFunction.INSTANCE, StringTypeActionIfAbsentRegisterFunction.INSTANCE, TaskIdentifierActionIfAbsentRegisterFunction.INSTANCE]
	//endregion

	static class TestableTask extends DefaultTask {}
	static class AnotherTestableTask extends DefaultTask {}
	static class WrongTask extends DefaultTask {}

	interface TestableComponent extends Component {}
	interface TestableVariant extends Variant {}
}
