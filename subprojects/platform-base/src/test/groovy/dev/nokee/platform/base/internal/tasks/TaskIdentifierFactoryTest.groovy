package dev.nokee.platform.base.internal.tasks


import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

@Subject(TaskIdentifierFactory)
class TaskIdentifierFactoryTest extends Specification {
	def "generates bare task names when direct parent is project"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new ProjectIdentifier('root'))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'foo'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'link'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateProto'
	}

	def "generates task names with component name when direct parent is component"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new ComponentIdentifier('test', new ProjectIdentifier('root')))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'fooTest'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'linkTest'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileTestC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateTestProto'
	}

	def "generates bare task names when direct parent is main component"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new ComponentIdentifier('main', new ProjectIdentifier('root')))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'foo'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'link'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateProto'
	}

	def "generates task names with component name and unambiguous variant name when direct parent is variant of a non-main component"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new VariantIdentifier('macosDebug', new ComponentIdentifier('test', new ProjectIdentifier('root'))))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'fooTestMacosDebug'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'linkTestMacosDebug'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileTestMacosDebugC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateTestMacosDebugProto'
	}

	def "generates task names without component name but with unambiguous variant name when direct parent is variant of a main component"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new VariantIdentifier('macosDebug', new ComponentIdentifier('main', new ProjectIdentifier('root'))))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'fooMacosDebug'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'linkMacosDebug'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileMacosDebugC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateMacosDebugProto'
	}

	def "generates task names with component name and without variant name when direct parent is variant of a non-main component without unambiguous variant name"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new VariantIdentifier('', new ComponentIdentifier('test', new ProjectIdentifier('root'))))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'fooTest'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'linkTest'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileTestC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateTestProto'
	}

	def "generates task names without component name and without variant name when direct parent is variant of a main component without unambiguous variant name"() {
		given:
		def subject = TaskIdentifierFactory.childOf(new VariantIdentifier('', new ComponentIdentifier('main', new ProjectIdentifier('root'))))

		expect:
		subject.create(TaskName.of('foo'), DummyTask).taskName == 'foo'
		subject.create(TaskName.of('link'), DummyTask).taskName == 'link'
		subject.create(TaskName.of('compile', 'c'), DummyTask).taskName == 'compileC'
		subject.create(TaskName.of('generate', 'proto'), DummyTask).taskName == 'generateProto'
	}

	interface DummyTask extends Task {}
}
