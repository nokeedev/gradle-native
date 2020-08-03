package dev.nokee.utils.internal

import org.gradle.api.Task
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Supplier

@Subject(AssertingTaskAction)
class AssertingTaskActionTest extends Specification {
	def "calls expression supplier only once"() {
		given:
		def expression = Mock(Supplier)

		when:
		new AssertingTaskAction(expression, "Error message").execute(Mock(Task))

		then:
		1 * expression.get() >> true
	}

	def "can use supplier as error message for lazy error message"() {
		given:
		def expression = Mock(Supplier)
		def errorMessage = Mock(Supplier)

		when:
		new AssertingTaskAction(expression, errorMessage).execute(Mock(Task))
		then:
		1 * expression.get() >> true
		0 * errorMessage.get()

		when:
		new AssertingTaskAction(expression, errorMessage).execute(Mock(Task))
		then:
		1 * expression.get() >> false
		1 * errorMessage.get() >> 'Computed error message'
		and:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Computed error message'
	}
}
