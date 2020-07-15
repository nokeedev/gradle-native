package dev.nokee.buildadapter.cmake.internal

import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Supplier

@Subject(DeferUtils)
class DeferUtilsTest extends Specification {
	def "can defer through Object#toString()"() {
		given:
		def supplier = Mock(Supplier)

		when:
		def result = DeferUtils.asToStringObject(supplier)
		then:
		0 * supplier.get()

		when:
		result.toString()
		then:
		1 * supplier.get()
	}

	def "call Supplier#get() via Object#toString()"() {
		given:
		def expectedData = UUID.randomUUID().toString()
		def supplier = { return expectedData }

		expect:
		DeferUtils.asToStringObject(supplier).toString() == expectedData
	}
}
