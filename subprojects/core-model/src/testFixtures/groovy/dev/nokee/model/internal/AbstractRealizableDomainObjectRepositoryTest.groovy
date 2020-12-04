package dev.nokee.model.internal

import dev.nokee.internal.testing.utils.TestUtils
import spock.lang.Shared

import java.util.function.Predicate

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.isDescendent

abstract class AbstractRealizableDomainObjectRepositoryTest<T> extends DomainObjectSpec<T> {
	@Shared def providerFactory = TestUtils.providerFactory()

	protected RealizableDomainObjectRepository<T> newSubject() {
		return newSubject(new RealizableDomainObjectRealizerImpl(eventPublisher))
	}

	protected abstract RealizableDomainObjectRepository<T> newSubject(RealizableDomainObjectRealizer realizer)

	def "throws exception when getting unknown entity identifier"() {
		given:
		def subject = newSubject()
		def identifier = entityIdentifier(ownerIdentifier)

		when:
		subject.get(identifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown entity identified as ${identifier}."
	}

	def "realizes the entity on get"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		subject.get(identifier)

		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier, entity))
	}

	def "can return entity realized during realization"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def result = subject.get(identifier)

		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier, entity))
		and:
		result == entity
	}

	def "realizes the filtered entity on get"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def owner1 = ownerIdentifier()
		def owner2 = ownerIdentifier()
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(owner1))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(owner2))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(owner1))))

		when:
		def result = subject.filter({ isDescendent(it, owner1) })

		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier1, entity1))
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier3, entity3))
		and:
		result == [entity1, entity3] as Set
	}

	def "retained discovered ordering"() {
		given:
		def subject = newSubject()

		and:
		def (identifier1, entity1) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier2, entity2) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier3, entity3) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier4, entity4) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))
		def (identifier5, entity5) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def result = subject.filter({ true })

		then:
		result as List == [entity1, entity2, entity3, entity4, entity5]
	}

	def "returns empty list when no entities match predicate"() {
		given:
		def subject = newSubject()

		and:
		def owner1 = ownerIdentifier()
		def owner2 = ownerIdentifier()
		def (identifier1, entity1) = entityRealized(entity(entityDiscovered(entityIdentifier(owner1))))
		def (identifier2, entity2) = entityRealized(entity(entityDiscovered(entityIdentifier(owner1))))
		def (identifier3, entity3) = entityRealized(entity(entityDiscovered(entityIdentifier(owner1))))

		when:
		def result = subject.filter({ isDescendent(it, owner2) })

		then:
		result == [] as Set
	}

	def "throws exception when identifying unknown entity"() {
		given:
		def subject = newSubject()
		def identifier = entityIdentifier(ownerIdentifier)

		when:
		subject.identified(identifier)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "Unknown entity identified as ${identifier}."
	}

	def "realize identified element only when provider is queried"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def provider = subject.identified(identifier)
		then:
		0 * realizedSubscriber.handle(_)

		when:
		def result = provider.get()
		then:
		1 * realizedSubscriber.handle(new DomainObjectRealized(identifier, entity))
		and:
		result == entity
	}

	def "can match identifier without realizing entity"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def hasFoundIdentifier = subject.anyKnownIdentifier({ it == identifier } as Predicate)

		then:
		0 * realizedSubscriber.handle(_)

		and:
		hasFoundIdentifier
	}

	def "can find a single identifier without realizing entity"() {
		given:
		def subject = newSubject()

		and:
		def realizedSubscriber = Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> DomainObjectRealized
		}
		eventPublisher.subscribe(realizedSubscriber)

		and:
		def (identifier, entity) = entityCreated(entity(entityDiscovered(entityIdentifier(ownerIdentifier))))

		when:
		def foundIdentifier = subject.findKnownIdentifier({ it == identifier } as Predicate)

		then:
		0 * realizedSubscriber.handle(_)

		and:
		foundIdentifier.present
		foundIdentifier.get() == identifier
	}
}
