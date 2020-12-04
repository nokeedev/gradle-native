package dev.nokee.platform.nativebase.internal.tasks

import dev.nokee.internal.testing.utils.TestUtils
import spock.lang.Specification
import spock.lang.Subject

@Subject(ObjectsLifecycleTask)
class ObjectsLifecycleTaskTest extends Specification {
	def tasks = TestUtils.rootProject().tasks

	def "preconfigures to build group"() {
		expect:
		tasks.create('foo', ObjectsLifecycleTask).group == 'build'
	}

	def "preconfigures description"() {
		expect:
		tasks.create('foo', ObjectsLifecycleTask).description == 'Assembles main objects.'
	}
}
