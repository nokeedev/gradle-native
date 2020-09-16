package dev.nokee.model.internal

import spock.lang.Subject

@Subject(Value)
class Value_FixedTest extends Value_AbstractTest {
	@Override
	def <T> Value<T> newSubject(T value) {
		return Value.fixed(value)
	}
}
