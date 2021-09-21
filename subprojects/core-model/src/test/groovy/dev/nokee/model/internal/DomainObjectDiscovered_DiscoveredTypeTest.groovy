/*
 * Copyright 2020-2021 the original author or authors.
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

import dev.nokee.utils.ActionUtils
import org.gradle.api.Action
import spock.lang.Specification

import static dev.nokee.model.internal.DomainObjectDiscovered.discoveredType

class DomainObjectDiscovered_DiscoveredTypeTest extends Specification {
	def "can create subscribe executing rule when specific type is discovered"() {
		given:
		def action = Mock(Action)
		def subject = discoveredType(MyEntity, action)
		def identifier = newIdentifier(MyEntity)

		when:
		subject.handle(new DomainObjectDiscovered<MyEntity>(identifier))
		then:
		1 * action.execute(identifier)

		when:
		subject.handle(new DomainObjectDiscovered(newIdentifier(MyOtherEntity)))
		then:
		0 * action.execute(_)
	}

	def "discovered type subscribe has a meaningful toString implementation"() {
		expect:
		discoveredType(MyEntity, ActionUtils.doNothing()).toString() == "DomainObjectDiscovered.discoveredType(${MyEntity.canonicalName}, ActionUtils.doNothing())"
	}

	protected <S> TypeAwareDomainObjectIdentifier<S> newIdentifier(Class<S> type) {
		return Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> type
		}
	}

	interface MyEntity {}
	interface MyOtherEntity {}
}
