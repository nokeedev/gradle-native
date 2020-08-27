package dev.nokee.platform.base.internal.tasks


import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(TaskIdentifier)
class TaskIdentifierTest extends Specification {
	@Unroll
	def "can compare identifier based on task name"(owner) {
		expect:
		new TaskIdentifier<>('foo', DummyTask, owner) == new TaskIdentifier<>('foo', DummyTask, owner)
		new TaskIdentifier<>('foo', DummyTask, owner) != new TaskIdentifier<>('bar', DummyTask, owner)

		where:
		owner << [new ProjectIdentifier('root'), new ComponentIdentifier('test', new ProjectIdentifier('root')), new VariantIdentifier('macosDebug', new ComponentIdentifier('test', new ProjectIdentifier('root')))]
	}

	@Unroll
	def "can compare identifier based on task type"(owner) {
		expect:
		new TaskIdentifier<>('foo', DummyTask, owner) == new TaskIdentifier<>('foo', DummyTask, owner)
		new TaskIdentifier<>('foo', DummyTask, owner) != new TaskIdentifier<>('foo', AnotherDummyTask, owner)

		where:
		owner << [new ProjectIdentifier('root'), new ComponentIdentifier('test', new ProjectIdentifier('root')), new VariantIdentifier('macosDebug', new ComponentIdentifier('test', new ProjectIdentifier('root')))]
	}

	def "can compare identifier based on owner"() {
		given:
		def ownerProject = new ProjectIdentifier('root')
		def ownerComponent = new ComponentIdentifier('test', ownerProject)
		def ownerVariant = new VariantIdentifier('macosDebug', ownerComponent)

		expect:
		new TaskIdentifier<>('foo', DummyTask, ownerProject) == new TaskIdentifier<>('foo', DummyTask, ownerProject)
		new TaskIdentifier<>('foo', DummyTask, ownerProject) != new TaskIdentifier<>('foo', DummyTask, ownerComponent)
		new TaskIdentifier<>('foo', DummyTask, ownerProject) != new TaskIdentifier<>('foo', DummyTask, ownerVariant)

		and:
		new TaskIdentifier<>('foo', DummyTask, ownerComponent) == new TaskIdentifier<>('foo', DummyTask, ownerComponent)
		new TaskIdentifier<>('foo', DummyTask, ownerComponent) != new TaskIdentifier<>('foo', DummyTask, ownerProject)
		new TaskIdentifier<>('foo', DummyTask, ownerComponent) != new TaskIdentifier<>('foo', DummyTask, ownerVariant)

		and:
		new TaskIdentifier<>('foo', DummyTask, ownerVariant) == new TaskIdentifier<>('foo', DummyTask, ownerVariant)
		new TaskIdentifier<>('foo', DummyTask, ownerVariant) != new TaskIdentifier<>('foo', DummyTask, ownerProject)
		new TaskIdentifier<>('foo', DummyTask, ownerVariant) != new TaskIdentifier<>('foo', DummyTask, ownerComponent)
	}

	interface DummyTask extends Task {}
	interface AnotherDummyTask extends Task {}
}
