package dev.nokee.utils

import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Supplier

@Subject(DeferUtils)
class DeferUtils_AsToStringObjectTest extends Specification {
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
