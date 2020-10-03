package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Consumer

@Subject(DomainObjects)
abstract class DomainObjectsSpec extends Specification {
	protected def eventPublisher = new DomainObjectEventPublisherImpl()

	protected def entityRealized(def object = null) {
		def identifier = Stub(DomainObjectIdentifierInternal)
		if (object == null) {
			object = Stub(MyEntity)
		}
		eventPublisher.publish(new DomainObjectRealized<>(identifier, object))
		return [identifier, object]
	}

	protected def entityCreated(def object = null) {
		def identifier = Stub(DomainObjectIdentifierInternal)
		if (object == null) {
			object = Stub(MyEntity)
		}
		eventPublisher.publish(new DomainObjectCreated<>(identifier, object))
		return [identifier, object]
	}

	interface MyEntity {}
	interface UnrelatedEntity {}
}

@Subject(DomainObjects)
abstract class DomainObjects_AbstractEventTest extends DomainObjectsSpec {
	protected abstract DomainObjects newSubject()

	protected abstract def eventUnderTestPublished(def object = null)
	protected abstract def unrelatedEventPublished()

	def "can create a subject capturing events"() {
		given:
		def subject = newSubject()

		and:
		def (DomainObjectIdentifier identifier, object) = eventUnderTestPublished()

		expect:
		subject.getByIdentifier(identifier) == object
	}

	def "does not capture unrelated events"() {
		given:
		def subject = newSubject()

		and:
		def (DomainObjectIdentifier identifier, object) = unrelatedEventPublished()

		when:
		subject.getByIdentifier(identifier)

		then:
		thrown(NullPointerException)
	}

	def "does not capture events of unrelated type"() {
		given:
		def subject = newSubject()

		and:
		def (DomainObjectIdentifier identifier, object) = eventUnderTestPublished(Stub(UnrelatedEntity))

		when:
		subject.getByIdentifier(identifier)

		then:
		thrown(NullPointerException)
	}

	def "iterate all captured event's object"() {
		given:
		def subject = newSubject()

		and:
		def (DomainObjectIdentifier identifier1, object1) = eventUnderTestPublished()
		def (DomainObjectIdentifier identifier2, object2) = eventUnderTestPublished()
		def (DomainObjectIdentifier identifier3, object3) = eventUnderTestPublished()

		and:
		def action = Mock(Consumer)

		when:
		subject.forEach(action)

		then:
		1 * action.accept(object1)
		and:
		1 * action.accept(object2)
		and:
		1 * action.accept(object3)
		0 * action.accept(_)
	}

	def "can reverse lookup identifier of captured object"() {
		given:
		def subject = newSubject()

		and:
		def (DomainObjectIdentifier identifier1, object1) = eventUnderTestPublished()
		def (DomainObjectIdentifier identifier2, object2) = eventUnderTestPublished()
		def (DomainObjectIdentifier identifier3, object3) = eventUnderTestPublished()

		expect:
		subject.lookupIdentifier(object1) == identifier1
		subject.lookupIdentifier(object2) == identifier2
		subject.lookupIdentifier(object3) == identifier3
	}

	def "throw exception when reverse lookup identifier for uncaptured object"() {
		given:
		def subject = newSubject()

		when:
		subject.lookupIdentifier(new Object())

		then:
		def ex = thrown(RuntimeException)
		ex.message == 'Unknown object'
	}
}

@Subject(DomainObjects)
class DomainObjects_CreatedEventTest extends DomainObjects_AbstractEventTest {
	@Override
	protected DomainObjects newSubject() {
		return new DomainObjects(MyEntity, eventPublisher, {})
	}

	@Override
	protected eventUnderTestPublished(def object) {
		return entityCreated(object)
	}

	@Override
	protected unrelatedEventPublished() {
		return entityRealized()
	}
}

@Subject(DomainObjects)
class DomainObjects_RealizedEventTest extends DomainObjects_AbstractEventTest {
	@Override
	protected DomainObjects newSubject() {
		return new DomainObjects(MyEntity, eventPublisher)
	}

	@Override
	protected eventUnderTestPublished(def object) {
		return entityRealized(object)
	}

	@Override
	protected unrelatedEventPublished() {
		return entityCreated()
	}
}

@Subject(DomainObjects)
class DomainObjectsTest extends DomainObjectsSpec {
	def "calls back on each entity created"() {
		given:
		def callback = Mock(Consumer)
		def subject = new DomainObjects(MyEntity, eventPublisher, callback)

		and:
		def object1 = Stub(MyEntity)
		def object2 = Stub(MyEntity)

		when:
		entityCreated(object1)
		then:
		1 * callback.accept(object1)

		when:
		entityCreated(object2)
		then:
		1 * callback.accept(object2)
	}

	def "does not call back on each unrelated entity created"() {
		given:
		def callback = Mock(Consumer)
		def subject = new DomainObjects(MyEntity, eventPublisher, callback)

		and:
		def object1 = Stub(UnrelatedEntity)
		def object2 = Stub(UnrelatedEntity)

		when:
		entityCreated(object1)
		then:
		0 * callback.accept(_)

		when:
		entityCreated(object2)
		then:
		0 * callback.accept(_)
	}
}
