package dev.nokee.platform.base.internal.tasks

import dev.nokee.model.internal.AbstractDomainObjectViewTest
import org.gradle.api.Buildable
import org.gradle.api.Task
import spock.lang.Subject

@Subject(TaskViewImpl)
class TaskViewTest extends AbstractDomainObjectViewTest<Task> implements TaskFixture {
	def "is buildable"() {
		expect:
		newSubject() instanceof Buildable
	}

	def "can depends on each tasks inside task view"() {
		given:
		def subject = newSubject()
		def registry = newTaskRegistry()

		and:
		def entityProvider1 = registry.register(entityIdentifier(ownerIdentifier))
		def entityProvider2 = registry.register(entityIdentifier(ownerIdentifier))
		def entityProvider3 = registry.register(entityIdentifier(ownerIdentifier))

		expect:
		subject.buildDependencies.getDependencies(null) == [entityProvider1.get(), entityProvider2.get(), entityProvider3.get()] as Set
	}
}
