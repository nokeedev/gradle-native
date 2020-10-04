package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

@Subject(RealizableDomainObjectRealizer)
class RealizableDomainObjectRealizerImplTest extends Specification {
	def eventPublisher = new DomainObjectEventPublisherImpl()

	protected DomainObjectIdentifierInternal identifier(DomainObjectIdentifierInternal parentIdentifier = null) {
		return Stub(DomainObjectIdentifierInternal) {
			getParentIdentifier() >> Optional.ofNullable(parentIdentifier)
		}
	}

	protected List<Object> realizableDiscovered(DomainObjectIdentifierInternal identifier, RealizableDomainObject realizableObject) {
		eventPublisher.publish(new RealizableDomainObjectDiscovered(identifier, realizableObject))
		return [identifier, realizableObject]
	}

	protected List created(DomainObjectIdentifier identifier) {
		return created(identifier, new Object())
	}

	protected List created(DomainObjectIdentifier identifier, Object obj) {
		eventPublisher.publish(new DomainObjectCreated<>(identifier, obj))
		return [identifier, obj]
	}

	protected <T extends DomainObjectEvent> DomainObjectEventSubscriber<T> mockSubscriber(Class<T> eventType) {
		return Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> eventType
		}
	}

	protected <T extends DomainObjectEvent> DomainObjectEventSubscriber<T> subscribed(DomainObjectEventSubscriber<T> subscriber) {
		eventPublisher.subscribe(subscriber)
		return subscriber
	}

	protected <T extends DomainObjectEvent> DomainObjectEventSubscriber<T> subscribed(Class<T> eventType) {
		return subscribed(mockSubscriber(eventType))
	}

	def "can realize no-parent identifier not known by the realizer"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)

		when:
		subject.ofElement(identifier())

		then:
		noExceptionThrown()
	}

	def "can realize identifier with parent not known by the realizer"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)

		when:
		subject.ofElement(identifier(identifier()))

		then:
		noExceptionThrown()
	}

	def "returns specified identifier"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)
		def identifier = identifier()

		expect:
		subject.ofElement(identifier) == identifier
	}

	def "calls realize callback of no-parent identifier"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)
		def (realizableIdentifier, realizableObject) = realizableDiscovered(identifier(), Mock(RealizableDomainObject))

		when:
		subject.ofElement(realizableIdentifier)

		then:
		1 * realizableObject.realize() >> { created(realizableIdentifier) }
	}

	def "calls realize callback of identifier and its parent starting from up-most parent"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)
		def (realizableIdentifier1, realizableObject1) = realizableDiscovered(identifier(), Mock(RealizableDomainObject))
		def (realizableIdentifier2, realizableObject2) = realizableDiscovered(identifier(realizableIdentifier1), Mock(RealizableDomainObject))
		def (realizableIdentifier3, realizableObject3) = realizableDiscovered(identifier(realizableIdentifier2), Mock(RealizableDomainObject))

		when:
		subject.ofElement(realizableIdentifier3)

		then:
		1 * realizableObject1.realize() >> { created(realizableIdentifier1) }
		and:
		1 * realizableObject2.realize() >> { created(realizableIdentifier2) }
		and:
		1 * realizableObject3.realize() >> { created(realizableIdentifier3) }
	}

	def "does not call unrelated realize callback"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)
		def (realizableIdentifier, realizableObject) = realizableDiscovered(identifier(), Mock(RealizableDomainObject))

		and:
		realizableDiscovered(identifier(), Mock(RealizableDomainObject))
		realizableDiscovered(identifier(), Mock(RealizableDomainObject))
		realizableDiscovered(identifier(), Mock(RealizableDomainObject))

		when:
		subject.ofElement(realizableIdentifier)

		then:
		1 * realizableObject.realize() >> { created(realizableIdentifier) }
		0 * _
	}

	def "throws exception if realized identifier is never created"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)
		def (realizableIdentifier, realizableObject) = realizableDiscovered(identifier(), Mock(RealizableDomainObject))

		when:
		subject.ofElement(realizableIdentifier)

		then:
		1 * realizableObject.realize()
		and:
		def ex = thrown(IllegalStateException)
		ex.message == "Element wasn't created"
	}

	def "publishes realized event when with the created object"() {
		given:
		def subject = new RealizableDomainObjectRealizerImpl(eventPublisher)
		def (realizableIdentifier, realizableObject) = realizableDiscovered(identifier(), Mock(RealizableDomainObject))
		def entity = new Object()

		and:
		def subscriber = subscribed(DomainObjectRealized)

		when:
		subject.ofElement(realizableIdentifier)

		then:
		1 * realizableObject.realize() >> { created(realizableIdentifier, entity)}
		1 * subscriber.handle(new DomainObjectRealized(realizableIdentifier, entity))
	}
}
