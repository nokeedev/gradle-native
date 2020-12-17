package dev.nokee.model.internal

import dev.nokee.internal.testing.utils.TestUtils
import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.Action
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

@Subject(NokeeMapImpl)
abstract class NokeeMap_AbstractCollectionTest extends Specification {
	@Shared def objectFactory = TestUtils.objectFactory()

	protected abstract def collectionUnderTest(NokeeMap subject)
	protected abstract def byTypeFilter(Class type)
	protected abstract def configureValue(Action action)
	protected abstract def asValues(Collection c)
	protected abstract def callbackWithValue(Action action)

	def "throws exception when adding new value to collection under test"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		collectionUnderTest(subject).add(Stub(Value))

		then:
		thrown(UnsupportedOperationException)
	}

	def "throws exception when realizing collection under test via getter without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		collectionUnderTest(subject).get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "throws exception when realizing collection under test via provider without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		collectionUnderTest(subject).getElements().get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "can realize collection under test via getter after disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		subject.disallowChanges()

		when:
		collectionUnderTest(subject).get()

		then:
		noExceptionThrown()
	}

	def "can realize collection under test via provider after disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		subject.disallowChanges()

		when:
		collectionUnderTest(subject).getElements().get()

		then:
		noExceptionThrown()
	}

	def "syncs collection under test size with map"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		when:
		def collection = collectionUnderTest(subject)
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

	def "can register action for each value in the collection under test for future entries"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = collectionUnderTest(subject)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value)

		when:
		collection.forEach(configureValue(Stub(Action)))
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

	def "can register action for each element in the collection under test for pass entries"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = collectionUnderTest(subject)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Mock(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Mock(Value)
		subject.put(k2, v2)

		when:
		collection.forEach(configureValue(Stub(Action)))
		then:
		noExceptionThrown()
		and:
		1 * v1.mapInPlace(_)
		and:
		1 * v2.mapInPlace(_)
	}

	def "realizing the collection under test via getter returns the elements"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = collectionUnderTest(subject)

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
		def result = asValues(collection.get())
		then:
		1 * v1.get() >> 'foo'
		and:
		1 * v2.get() >> 'bar'
		and:
		result == ['foo', 'bar']
	}

	def "realizing the collection under test via provider returns the values"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)
		def collection = collectionUnderTest(subject)
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
		def result = asValues(provider.get())
		then:
		1 * v1.get() >> 'foo'
		and:
		1 * v2.get() >> 'bar'
		and:
		result == ['foo', 'bar']
	}

	def "can filter collection under test by type"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)
		def filteredCollection = collection.filter(byTypeFilter(B))

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
		asValues(filteredCollection.get()) == [b]
	}

	def "can register for each action on filter collection under test by type"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)
		def filteredCollection = collection.filter(byTypeFilter(B))

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
		filteredCollection.forEach(configureValue(Stub(Action)))

		then:
		0 * v1.mapInPlace(_)
		1 * v2.mapInPlace(_)
	}

	def "throws exception when realizing filtered collection under test via getter without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)
		def filteredCollection = collection.filter(byTypeFilter(B))

		when:
		filteredCollection.get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "throws exception when realizing filtered collection under test via provider without disallowing changes on map"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)
		def filteredCollection = collection.filter(byTypeFilter(B))

		when:
		filteredCollection.getElements().get()

		then:
		def ex = thrown(IllegalStateException)
		ex.message == 'Please disallow changes before realizing this collection.'
	}

	def "syncs filtered collection under test size with map according to filter"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)

		and:
		def a = Stub(Value) {
			getType() >> A
		}
		def b = Stub(Value) {
			getType() >> B
		}

		when:
		def filteredCollection = collection.filter(byTypeFilter(B))
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

	def "can register when element added on collection under test for future elements"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)

		and:
		def action = Mock(Action)

		when:
		collection.whenElementAdded(callbackWithValue(action))
		then:
		0 * action.execute(_)

		when:
		subject.put(k1, v1)
		then:
		1 * action.execute(v1)

		when:
		subject.put(k2, v2)
		then:
		1 * action.execute(v2)
	}

	def "calls when element added for previous values on collection under test"() {
		given:
		def subject = new NokeeMapImpl(A, objectFactory)
		def collection = collectionUnderTest(subject)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)
		subject.put(k2, v2)

		and:
		def action = Mock(Action)

		when:
		collection.whenElementAdded(callbackWithValue(action))

		then:
		1 * action.execute(v1)

		and:
		1 * action.execute(v2)
	}

	class A {}
	class B extends A {}
}
