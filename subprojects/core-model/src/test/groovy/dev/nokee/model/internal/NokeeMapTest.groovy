package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@Subject(NokeeMapImpl)
class NokeeMapTest extends Specification {
	@Shared def objectFactory = ProjectBuilder.builder().build().objects

	def "can put new entry in the map"() {
		when:
		new NokeeMapImpl(String, objectFactory).put(Stub(DomainObjectIdentifier), Stub(Value))

		then:
		noExceptionThrown()
	}

	def "is zero-size on creation"() {
		when:
		def subject = new NokeeMapImpl(String, objectFactory)

		then:
		subject.size() == 0
	}

	def "putting new entry increase size by one"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		subject.put(Stub(DomainObjectIdentifier), Stub(Value))
		then:
		subject.size() == 1

		when:
		subject.put(Stub(DomainObjectIdentifier), Stub(Value))
		then:
		subject.size() == 2
	}

	def "can get previously put value"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def identifier = Stub(DomainObjectIdentifier)
		def value = Stub(Value)

		when:
		subject.put(identifier, value)

		then:
		subject.get(identifier) == value
	}

	def "throws exception when putting new value after disallowing changes"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		subject.disallowChanges()
		subject.put(Stub(DomainObjectIdentifier), Stub(Value))

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'The value cannot be changed any further.'
	}

	def "returns the map instance when disallowing changes"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		expect:
		subject.disallowChanges() == subject
	}

	def "can get a collection of the values"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		expect:
		subject.values() instanceof NokeeCollection
	}

	def "can register forEach action triggering on pass entries"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)
		subject.put(k2, v2)

		when:
		def capturedKeys = []
		def capturedValues = []
		subject.forEach { k, v ->
			capturedKeys << k
			capturedValues << v
		}

		then:
		capturedKeys == [k1, k2]
		capturedValues == [v1, v2]
	}

	def "can register forEach action triggering on future entries"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)

		when:
		def capturedKeys = []
		def capturedValues = []
		subject.forEach { k, v ->
			capturedKeys << k
			capturedValues << v
		}
		then:
		capturedKeys == []
		capturedValues == []

		when:
		subject.put(k1, v1)
		then:
		capturedKeys == [k1]
		capturedValues == [v1]

		when:
		subject.put(k2, v2)
		then:
		capturedKeys == [k1, k2]
		capturedValues == [v1, v2]
	}

	def "values are not prematuraly realized"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def value = Mock(Value)
		def identifier = Stub(DomainObjectIdentifier)

		when:
		subject.put(identifier, value)
		subject.values()
		subject.size()
		subject.get(identifier)
		subject.forEach {k, v -> }
		subject.disallowChanges()

		then:
		0 * value.get()
	}
}
