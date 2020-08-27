package dev.nokee.platform.base.internal.tasks

import org.gradle.api.Action
import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

@Subject(KnownTaskIdentifierActionAdapter)
class KnownTaskIdentifierActionAdapterTest extends Specification {
	def "forwards execution to action if task name is known"() {
		given:
		def knownTaskIdentifiers = Mock(KnownTaskIdentifiers)
		def action = Mock(Action)
		def task = Mock(Task)
		def subject = new KnownTaskIdentifierActionAdapter(knownTaskIdentifiers, action)

		when:
		subject.execute(task)

		then:
		1 * knownTaskIdentifiers.contains('foo') >> true
		1 * task.name >> 'foo'
		1 * action.execute(task)
		0 * _
	}

	def "does not forward execution to action if task name is unknown"() {
		given:
		def knownTaskIdentifiers = Mock(KnownTaskIdentifiers)
		def action = Mock(Action)
		def task = Mock(Task)
		def subject = new KnownTaskIdentifierActionAdapter(knownTaskIdentifiers, action)

		when:
		subject.execute(task)

		then:
		1 * knownTaskIdentifiers.contains('bar') >> false
		1 * task.name >> 'bar'
		0 * action.execute(task)
		0 * _
	}
}
