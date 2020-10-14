package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject

@Subject(RealizableGradleProvider)
class RealizableGradleProviderTest extends Specification {
	protected RealizableGradleProvider newSubject(Provider provider) {
		return new RealizableGradleProvider(Stub(DomainObjectIdentifier), provider, Stub(DomainObjectEventPublisher))
	}

	protected RealizableGradleProvider newSubject(Provider provider, DomainObjectIdentifier identifier) {
		return new RealizableGradleProvider(identifier, provider, Stub(DomainObjectEventPublisher))
	}

	def "can realize Gradle provider"() {
		given:
		def provider = Mock(Provider)
		def subject = newSubject(provider)

		when:
		subject.realize()

		then:
		1 * provider.get()
		0 * _
	}

	def "can compare realizable Gradle provider"() {
		given:
		def identifier1 = Stub(DomainObjectIdentifier)
		def provider1 = Stub(Provider)

		and:
		def identifier2 = Stub(DomainObjectIdentifier)
		def provider2 = Stub(Provider)

		expect:
		newSubject(provider1, identifier1) == newSubject(provider1, identifier1)
		newSubject(provider2, identifier2) == newSubject(provider2, identifier2)

		and:
		newSubject(provider1, identifier1) != newSubject(provider2, identifier2)
	}

	def "publishes an entity created event upon realization"() {
		given:
		def eventPublisher = Mock(DomainObjectEventPublisher)
		def entityIdentifier = Stub(DomainObjectIdentifier)
		def entity = new Object()
		def provider = Stub(Provider) {
			get() >> entity
		}
		def subject = new RealizableGradleProvider(entityIdentifier, provider, eventPublisher)

		when:
		subject.realize()

		then:
		1 * eventPublisher.publish(new DomainObjectCreated(entityIdentifier, entity))
	}
}
