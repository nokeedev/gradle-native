package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.Action
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer

@Subject(KnownDomainObjectActions)
class KnownDomainObjectActionsTest extends Specification {
	def subject = new KnownDomainObjectActions()

	protected Consumer actionAdded(Consumer action) {
		subject.add(action)
		return action
	}

	protected MyIdentifier identifier(DomainObjectIdentifier owner, Class type) {
		return Stub(MyIdentifier) {
			getType() >> type
			getParentIdentifier() >> Optional.of(owner)
		}
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
		def ownerIdentifier = Stub(DomainObjectIdentifierInternal)
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
