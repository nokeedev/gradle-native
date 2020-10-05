package dev.nokee.model.internal

import org.gradle.api.provider.Provider
import spock.lang.Specification
import spock.lang.Subject

@Subject(RealizableGradleProvider)
class RealizableGradleProviderTest extends Specification {
	def "can realize Gradle provider"() {
		given:
		def provider = Mock(Provider)
		def subject = new RealizableGradleProvider(provider)

		when:
		subject.realize()

		then:
		1 * provider.get()
		0 * _
	}

	def "can compare realizable Gradle provider"() {
		given:
		def provider1 = Stub(Provider)
		def provider2 = Stub(Provider)

		expect:
		new RealizableGradleProvider(provider1) == new RealizableGradleProvider(provider1)
		new RealizableGradleProvider(provider2) == new RealizableGradleProvider(provider2)

		and:
		new RealizableGradleProvider(provider1) != new RealizableGradleProvider(provider2)
	}
}
