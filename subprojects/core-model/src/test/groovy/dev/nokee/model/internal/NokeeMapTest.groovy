package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.Action
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.SpecUtils.byType

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

	def "throws exception when adding new value to value collection"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		subject.values().add(Stub(Value))

		then:
		thrown(UnsupportedOperationException)
	}

	def "throws exception when realizing value collection via getter without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		subject.values().get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "throws exception when realizing value collection via provider without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		subject.values().getElements().get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "can realize value collection via getter after disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		subject.disallowChanges()

		when:
		subject.values().get()

		then:
		noExceptionThrown()
	}

	def "can realize value collection via provider after disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		subject.disallowChanges()

		when:
		subject.values().getElements().get()

		then:
		noExceptionThrown()
	}

	def "sync value collection size with map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		def collection = subject.values()
		then:
		collection.size() == subject.size()

		when:
		subject.put(Stub(DomainObjectIdentifier), Stub(Value))
		then:
		collection.size() == subject.size()

		when:
		subject.put(Stub(DomainObjectIdentifier), Stub(Value))
		then:
		collection.size() == subject.size()
	}

	def "can register action for each value in the value collection for future entries"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = subject.values()

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value)

		when:
		collection.forEach(Stub(Action))
		then:
		noExceptionThrown()

		when:
		subject.put(k1, v1)
		then:
		1 * v1.mapInPlace(_)

		when:
		subject.put(k2, v2)
		then:
		1 * v2.mapInPlace(_)
	}

	def "can register action for each value in the value collection for pass entries"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = subject.values()

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value)
		subject.put(k2, v2)

		when:
		collection.forEach(Stub(Action))
		then:
		noExceptionThrown()
		and:
		1 * v1.mapInPlace(_)
		and:
		1 * v2.mapInPlace(_)
	}

	def "realizing the value collection via getter returns the values"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = subject.values()

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value)
		subject.put(k2, v2)

		and:
		subject.disallowChanges()

		when:
		def result = collection.get()
		then:
		1 * v1.get() >> 'foo'
		and:
		1 * v2.get() >> 'bar'
		and:
		result == ['foo', 'bar']
	}

	def "realizing the value collection via provider returns the values"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = subject.values()
		def provider = collection.getElements()

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value)
		subject.put(k2, v2)

		and:
		subject.disallowChanges()

		when:
		def result = provider.get()
		then:
		1 * v1.get() >> 'foo'
		and:
		1 * v2.get() >> 'bar'
		and:
		result == ['foo', 'bar']
	}

	def "can filter value collection by type"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = subject.values()
		def filteredCollection = collection.filter(byType(B))

		and:
		def a = new A()
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Value.fixed(a)
		subject.put(k1, v1)

		and:
		def b = new B()
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Value.fixed(b)
		subject.put(k2, v2)

		and:
		subject.disallowChanges()

		expect:
		filteredCollection.get() == [b]
	}

	def "can register for each action on filter value collection by type"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = subject.values()
		def filteredCollection = collection.filter(byType(B))

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value) {
			getType() >> A
		}
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value) {
			getType() >> B
		}
		subject.put(k2, v2)

		and:
		subject.disallowChanges()

		when:
		filteredCollection.forEach(Stub(Action))

		then:
		0 * v1.mapInPlace(_)
		1 * v2.mapInPlace(_)
	}

	def "throws exception when realizing filtered value collection via getter without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = subject.values()
		def filteredCollection = collection.filter(byType(B))

		when:
		filteredCollection.get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "throws exception when realizing filtered value collection via provider without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = subject.values()
		def filteredCollection = collection.filter(byType(B))

		when:
		filteredCollection.getElements().get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "syncs filtered value collection size with map according to filter"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = subject.values()

		and:
		def a = Stub(Value) {
			getType() >> A
		}
		def b = Stub(Value) {
			getType() >> B
		}

		when:
		def filteredCollection = collection.filter(byType(B))
		then:
		filteredCollection.size() == subject.size()

		when:
		subject.put(Stub(DomainObjectIdentifier), b)
		then:
		filteredCollection.size() == subject.size()

		when:
		subject.put(Stub(DomainObjectIdentifier), a)
		then:
		filteredCollection.size() == subject.size() - 1
	}

	class A {}
	class B extends A {}
}
