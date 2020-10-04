package dev.nokee.model.internal

import spock.lang.Specification
import spock.lang.Subject

@Subject(DomainObjectEventPublisherImpl)
class DomainObjectEventPublisherImplTest extends Specification {
	def subject = new DomainObjectEventPublisherImpl()

	protected <T extends DomainObjectEvent> DomainObjectEventSubscriber<T> mockSubscriber(Class<T> eventType) {
		return Mock(DomainObjectEventSubscriber) {
			subscribedToEventType() >> eventType
		}
	}

	protected <T extends DomainObjectEvent> DomainObjectEventSubscriber<T> subscribed(DomainObjectEventSubscriber<T> subscriber) {
		subject.subscribe(subscriber)
		return subscriber
	}

	protected <T extends DomainObjectEvent> DomainObjectEventSubscriber<T> subscribed(Class<T> eventType) {
		return subscribed(mockSubscriber(eventType))
	}

	def "can publish event without subscriber"() {
		when:
		subject.publish(Stub(DomainObjectEvent))

		then:
		noExceptionThrown()
	}

	def "calls registered subscriber for publishing event"() {
		given:
		def subscriber = subscribed(DomainObjectEvent)

		and:
		def event = Stub(DomainObjectEvent)

		when:
		subject.publish(event)

		then:
		1 * subscriber.handle(event)
	}

	def "does not call unrelated registered subscriber for publishing event"() {
		given:
		def subscriber1 = subscribed(MyEvent)
		def subscriber2 = subscribed(MyOtherEvent)

		and:
		def event = Stub(MyEvent)

		when:
		subject.publish(event)

		then:
		1 * subscriber1.handle(event)
		0 * subscriber2.handle(_)
	}

	def "calls registered subscriber for publishing event in order they were registered"() {
		given:
		def subscriber1 = subscribed(MyEvent)
		def subscriber2 = subscribed(MyEvent)
		def subscriber3 = subscribed(MyEvent)
		def subscriber4 = subscribed(MyEvent)

		and:
		def event = Stub(MyEvent)

		when:
		subject.publish(event)

		then:
		1 * subscriber1.handle(event)
		and:
		1 * subscriber2.handle(event)
		and:
		1 * subscriber3.handle(event)
		and:
		1 * subscriber4.handle(event)
	}

	def "calls registered subscriber for publishing event in order they were registered for different matching type"() {
		given:
		def subscriber1 = subscribed(MyEvent)
		def subscriber2 = subscribed(MyOtherEvent)
		def subscriber3 = subscribed(MyEvent)
		def subscriber4 = subscribed(DomainObjectEvent)

		and:
		def event = Stub(MyCombinedEvent)

		when:
		subject.publish(event)

		then:
		1 * subscriber1.handle(event)
		and:
		1 * subscriber2.handle(event)
		and:
		1 * subscriber3.handle(event)
		and:
		1 * subscriber4.handle(event)
	}

	def "throws exception when subscriber is null"() {
		when:
		subject.subscribe(null)

		then:
		thrown(NullPointerException)
	}

	def "throws exception when event is null"() {
		when:
		subject.publish(null)

		then:
		thrown(NullPointerException)
	}

	interface MyEvent extends DomainObjectEvent {}
	interface MyOtherEvent extends DomainObjectEvent {}
	interface MyCombinedEvent extends DomainObjectEvent, MyEvent, MyOtherEvent {}
}
