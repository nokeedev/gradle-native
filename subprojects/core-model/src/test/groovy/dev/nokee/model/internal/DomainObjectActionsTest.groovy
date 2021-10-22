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

@Subject(DomainObjectActions)
class DomainObjectActionsTest extends Specification {
	def subject = new DomainObjectActions()

	protected Consumer actionAdded(Consumer action) {
		subject.add(action)
		return action
	}

	protected DomainObjectIdentifier identifier(DomainObjectIdentifier owner) {
		def result = Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.of(owner)
		}
		result.iterator() >> { Stream.of(owner, result).iterator() }
		return result
	}

	def "can execute empty action list"() {
		when:
		subject.accept(new Object())

		then:
		noExceptionThrown()
	}

	def "can add configuration actions"() {
		given:
		def action1 = actionAdded(Mock(Consumer))
		def action2 = actionAdded(Mock(Consumer))
		def action3 = actionAdded(Mock(Consumer))

		and:
		def object = new Object()

		when:
		subject.accept(object)

		then:
		1 * action1.accept(object)
		and:
		1 * action2.accept(object)
		and:
		1 * action3.accept(object)
		0 * _
	}

	def "can filter object per type"() {
		given:
		def action = Mock(Consumer)
		Consumer<A> onlyIfAction = DomainObjectActions.onlyIf(B, action)

		and:
		def a = Stub(A)
		def b = Stub(B)

		when:
		onlyIfAction.accept(a)
		then:
		0 * action.accept(_)

		when:
		onlyIfAction.accept(b)
		then:
		1 * action.accept(b)
	}

	def "can filter object per identifier owner"() {
		given:
		def ownerIdentifier = Stub(DomainObjectIdentifierInternal)
		ownerIdentifier.iterator() >> { Stream.of(ownerIdentifier).iterator() }
		def ownedIdentifier = identifier(ownerIdentifier)
		def notOwnedIdentifier = identifier(Stub(DomainObjectIdentifierInternal))

		and:
		def action = Mock(Action)
		def onlyIfAction = DomainObjectActions.onlyIf(ownerIdentifier, action)

		and:
		def object = new Object()

		when:
		onlyIfAction.accept(ownedIdentifier, object)
		then:
		1 * action.execute(object)

		when:
		onlyIfAction.accept(notOwnedIdentifier, object)
		then:
		0 * action.execute(_)
	}

	interface A {}
	interface B extends A {}
}
