/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import java.util.stream.Stream

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.descendentOf

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_DescendentOfTest extends Specification {
	def "returns false for same identifier"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!descendentOf(identifier).test(identifier)
	}

	def "returns true when identifier is direct child"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifier)
		parentIdentifier.iterator() >> { Stream.of(parentIdentifier).iterator() }
		def childIdentifier = Stub(DomainObjectIdentifier)
		childIdentifier.iterator() >> { Stream.of(parentIdentifier, childIdentifier).iterator() }
		def anotherChildIdentifier = Stub(DomainObjectIdentifier)
		anotherChildIdentifier.iterator() >> { Stream.of(parentIdentifier, anotherChildIdentifier).iterator() }

		expect:
		descendentOf(parentIdentifier).test(childIdentifier)
		descendentOf(parentIdentifier).test(anotherChildIdentifier)
	}

	def "returns true when identifier is not a direct child"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifier)
		parentIdentifier.iterator() >> { Stream.of(parentIdentifier).iterator() }
		def indirectIdentifier = Stub(DomainObjectIdentifier)
		indirectIdentifier.iterator() >> { Stream.of(parentIdentifier, indirectIdentifier).iterator() }
		def childIdentifier = Stub(DomainObjectIdentifier)
		childIdentifier.iterator() >> { Stream.of(parentIdentifier, indirectIdentifier, childIdentifier).iterator() }
		def anotherChildIdentifier = Stub(DomainObjectIdentifier)
		anotherChildIdentifier.iterator() >> { Stream.of(parentIdentifier, indirectIdentifier, anotherChildIdentifier).iterator() }

		expect:
		descendentOf(parentIdentifier).test(childIdentifier)
		descendentOf(parentIdentifier).test(anotherChildIdentifier)
	}

	def "returns false when identifier is not descendent"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifier)
		def childIdentifier = Stub(DomainObjectIdentifier)
		def anotherChildIdentifier = Stub(DomainObjectIdentifier)

		expect:
		!descendentOf(childIdentifier).test(parentIdentifier)
		!descendentOf(anotherChildIdentifier).test(parentIdentifier)
	}

	def "returns false for non-internal identifier type"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifier)
		def childIdentifier = Stub(DomainObjectIdentifier)
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!descendentOf(childIdentifier).test(identifier)
		!descendentOf(parentIdentifier).test(identifier)
	}

	def "can compare predicates"() {
		given:
		def identifier1 = Stub(DomainObjectIdentifier)
		def identifier2 = Stub(DomainObjectIdentifier)

		expect:
		descendentOf(identifier1) == descendentOf(identifier1)
		descendentOf(identifier1) != descendentOf(identifier2)
	}

	def "predicate toString() explains where it comes from"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		descendentOf(identifier).toString() == "DomainObjectIdentifierUtils.descendentOf(${identifier.toString()})"
	}
}
