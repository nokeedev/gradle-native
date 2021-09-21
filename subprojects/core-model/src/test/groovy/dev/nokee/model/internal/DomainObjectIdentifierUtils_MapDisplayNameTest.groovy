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


import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.mapDisplayName

@Subject(DomainObjectIdentifierUtils)
class DomainObjectIdentifierUtils_MapDisplayNameTest extends Specification {
	def "can compare suppliers"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal)

		expect:
		mapDisplayName(identifier) == mapDisplayName(identifier)
		mapDisplayName(identifier) != mapDisplayName(Stub(DomainObjectIdentifierInternal))
	}

	def "returns identifier display name when getting supplier"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal) {
			getDisplayName() >> 'some display name'
		}

		expect:
		mapDisplayName(identifier).get() == 'some display name'
	}

	def "supplier toString() explains where it comes from"() {
		given:
		def identifier = Stub(DomainObjectIdentifierInternal)

		expect:
		mapDisplayName(identifier).toString() == "DomainObjectIdentifierUtils.mapDisplayName(${identifier.toString()})"
	}
}
