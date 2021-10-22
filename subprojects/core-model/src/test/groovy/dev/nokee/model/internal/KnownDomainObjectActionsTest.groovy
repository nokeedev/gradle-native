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
import org.gradle.api.Action
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer
import java.util.stream.Stream

@Subject(KnownDomainObjectActions)
class KnownDomainObjectActionsTest extends Specification {
	def subject = new KnownDomainObjectActions()

	protected Consumer actionAdded(Consumer action) {
		subject.add(action)
		return action
	}

	protected MyIdentifier identifier(DomainObjectIdentifier owner, Class type) {
		def result = Stub(MyIdentifier) {
			getType() >> type
			getParentIdentifier() >> Optional.of(owner)
		}
		result.iterator() >> { Stream.of(owner, result).iterator() }
		return result
	}

	def "can execute empty action list"() {
		when:
		subject.accept(Stub(TypeAwareDomainObjectIdentifier))

		then:
		noExceptionThrown()
	}

	def "can add configuration actions"() {
		given:
		def action1 = actionAdded(Mock(Consumer))
		def action2 = actionAdded(Mock(Consumer))
		def action3 = actionAdded(Mock(Consumer))

		and:
		def identifier = Stub(TypeAwareDomainObjectIdentifier)

		when:
		subject.accept(identifier)

		then:
		1 * action1.accept(identifier)
		and:
		1 * action2.accept(identifier)
		and:
		1 * action3.accept(identifier)
		0 * _
	}

	def "can filter object per owner and type"() {
		given:
		def ownerIdentifier = Stub(DomainObjectIdentifier)
		ownerIdentifier.iterator() >> { Stream.of(ownerIdentifier).iterator() }
		def ownedIdentifierOfCorrectType = identifier(ownerIdentifier, B)

		and:
		def action = Mock(Action)
		def onlyIfAction = KnownDomainObjectActions.onlyIf(ownerIdentifier, B, action)

		when:
		onlyIfAction.accept(identifier(ownerIdentifier, A))
		then:
		0 * action.execute(_)

		when:
		onlyIfAction.accept(ownedIdentifierOfCorrectType)
		then:
		1 * action.execute(ownedIdentifierOfCorrectType)

		when:
		onlyIfAction.accept(identifier(Stub(DomainObjectIdentifierInternal), B))
		then:
		0 * action.execute(_)

		when:
		onlyIfAction.accept(identifier(Stub(DomainObjectIdentifierInternal), A))
		then:
		0 * action.execute(_)
	}

	interface MyIdentifier extends TypeAwareDomainObjectIdentifier, DomainObjectIdentifierInternal {}
	interface A {}
	interface B extends A {}
}
