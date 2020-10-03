package dev.nokee.model.internal


import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer
import java.util.function.Predicate

@Subject(KnownDomainObjects)
class KnownDomainObjectsTest extends Specification {
	def eventPublisher = new DomainObjectEventPublisherImpl()

	protected KnownDomainObjects newSubject() {
		return new KnownDomainObjects(MyEntity, eventPublisher)
	}

	protected KnownDomainObjects newSubject(Consumer action) {
		return new KnownDomainObjects(MyEntity, eventPublisher, action)
	}

	protected def identifier(Class entityType) {
		return Stub(TypeAwareDomainObjectIdentifier) {
			getType() >> entityType
			toString() >> "entity '${entityType.simpleName}'"
		}
	}

	protected def entityDiscovered(TypeAwareDomainObjectIdentifier identifier) {
		eventPublisher.publish(new DomainObjectDiscovered<>(identifier))
		return identifier
	}

	def "identifier are known only when discovered"() {
		given:
		def subject = newSubject()
		def entityIdentifier = entityDiscovered(identifier(MyEntity))

		expect:
		subject.isKnown(entityIdentifier)
		!subject.isKnown(identifier(MyEntity))
		!subject.isKnown(identifier(UnrelatedEntity))
	}

	def "can assert an identifier is discovered"() {
		given:
		def subject = newSubject()
		def entityIdentifier = entityDiscovered(identifier(MyEntity))

		when:
		subject.assertKnownObject(entityIdentifier)
		then:
		noExceptionThrown()

		when:
		subject.assertKnownObject(identifier(MyEntity))
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown entity identified as entity 'MyEntity'."
	}

	def "does not know about discovery of unrelated entity type"() {
		given:
		def subject = newSubject()
		def entityIdentifier = entityDiscovered(identifier(UnrelatedEntity))

		expect:
		!subject.isKnown(entityIdentifier)
	}

	def "throws exception when asserting unrelated entity type identifier"() {
		given:
		def subject = newSubject()
		def entityIdentifier = entityDiscovered(identifier(UnrelatedEntity))

		when:
		subject.assertKnownObject(entityIdentifier)
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown entity identified as entity 'UnrelatedEntity'."
	}

	def "can filter discovered entities by identifier"() {
		given:
		def subject = newSubject()
		def entityIdentifier1 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier2 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier3 = entityDiscovered(identifier(MyEntity))

		expect:
		subject.filter { it == entityIdentifier2 } == [entityIdentifier2] as Set
	}

	def "filtered identifier retain discovered ordering"() {
		given:
		def subject = newSubject()
		def entityIdentifier1 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier2 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier3 = entityDiscovered(identifier(MyEntity))

		expect:
		subject.filter { it in [entityIdentifier1, entityIdentifier3] } as List == [entityIdentifier1, entityIdentifier3]
	}

	def "can execute action for each discovered identifier"() {
		given:
		def subject = newSubject()
		def entityIdentifier1 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier2 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier3 = entityDiscovered(identifier(MyEntity))

		and:
		def action = Mock(Consumer)

		when:
		subject.forEach(action)

		then:
		1 * action.accept(entityIdentifier1)
		and:
		1 * action.accept(entityIdentifier2)
		and:
		1 * action.accept(entityIdentifier3)
		0 * action._
	}

	def "can find identifier with predicate"() {
		given:
		def subject = newSubject()
		def entityIdentifier1 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier2 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier3 = entityDiscovered(identifier(MyEntity))

		when:
		def result = subject.find({ it == entityIdentifier2 } as Predicate)

		then:
		result.present
		result.get() == entityIdentifier2
	}

	def "throws exception if more than one identifier match finding predicate"() {
		given:
		def subject = newSubject()
		def entityIdentifier1 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier2 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier3 = entityDiscovered(identifier(MyEntity))

		when:
		subject.find({ true } as Predicate)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "expected one element but was: <entity 'MyEntity', entity 'MyEntity', entity 'MyEntity'>"
	}

	def "returns empty optional if none of the identifier match the finding predicate"() {
		given:
		def subject = newSubject()
		def entityIdentifier1 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier2 = entityDiscovered(identifier(MyEntity))
		def entityIdentifier3 = entityDiscovered(identifier(MyEntity))

		when:
		def result = subject.find({ false } as Predicate)

		then:
		!result.present
	}

	def "calls back for each entity discovered"() {
		given:
		def action = Mock(Consumer)
		def subject = newSubject(action)

		and:
		def entityIdentifier1 = identifier(MyEntity)
		def entityIdentifier2 = identifier(MyEntity)
		def entityIdentifier3 = identifier(MyEntity)

		when:
		entityDiscovered(entityIdentifier1)
		entityDiscovered(entityIdentifier2)
		entityDiscovered(entityIdentifier3)

		then:
		1 * action.accept(entityIdentifier1)
		and:
		1 * action.accept(entityIdentifier2)
		and:
		1 * action.accept(entityIdentifier3)
		0 * action.accept(_)
	}

	def "does not callback for each unrelated entity discovered"() {
		given:
		def action = Mock(Consumer)
		def subject = newSubject(action)

		and:
		def entityIdentifier1 = identifier(UnrelatedEntity)
		def entityIdentifier2 = identifier(UnrelatedEntity)
		def entityIdentifier3 = identifier(UnrelatedEntity)

		when:
		entityDiscovered(entityIdentifier1)
		entityDiscovered(entityIdentifier2)
		entityDiscovered(entityIdentifier3)

		then:
		0 * action.accept(_)
	}

	interface MyEntity {}
	interface UnrelatedEntity {}
}
