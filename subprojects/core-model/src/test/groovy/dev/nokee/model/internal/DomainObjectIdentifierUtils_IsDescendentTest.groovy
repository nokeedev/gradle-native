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

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_IsDescendentTest extends Specification {
	def "returns false for same identifier"() {
		given:
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!isDescendent(identifier, identifier)
	}

	def "returns true when identifier is direct child"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def anotherChildIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}

		expect:
		isDescendent(childIdentifier, parentIdentifier)
		isDescendent(anotherChildIdentifier, parentIdentifier)
	}

	def "returns true when identifier is not a direct child"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def indirectIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(indirectIdentifier)
		}
		def anotherChildIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(indirectIdentifier)
		}

		expect:
		isDescendent(childIdentifier, parentIdentifier)
		isDescendent(anotherChildIdentifier, parentIdentifier)
	}

	def "returns false when identifier is not descendent"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def anotherChildIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}

		expect:
		!isDescendent(parentIdentifier, childIdentifier)
		!isDescendent(parentIdentifier, anotherChildIdentifier)
	}

	def "returns false for non-internal identifier type"() {
		given:
		def parentIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.empty()
		}
		def childIdentifier = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(parentIdentifier)
		}
		def identifier = Stub(DomainObjectIdentifier)

		expect:
		!isDescendent(identifier, childIdentifier)
		!isDescendent(identifier, parentIdentifier)
	}
}
