package dev.nokee.model.internal

import spock.lang.Subject

import java.util.function.Supplier

@Subject(Value)
class Value_SuppliedTest extends Value_AbstractTest {
	@Override
	def <T> Value<T> newSubject(T value) {
		return Value.supplied({value})
	}

	def "memoize the supplied value on get"() {
		given:
		def supplier = Mock(Supplier)
		def subject = Value.supplied(supplier)

		when:
		def result1 = subject.get()
		then:
		1 * supplier.get() >> 42
		result1 == 42

		when:
		def result2 = subject.get()
		then:
		0 * supplier.get()
		result2 == 42
	}
}
