package dev.nokee.model.internal

import dev.nokee.model.DomainObjectIdentifier
import org.gradle.api.Action
import spock.lang.Subject

import static dev.nokee.utils.TransformerUtils.configureInPlace

@Subject(NokeeMapImpl)
class NokeeMap_EntrySetTest extends NokeeMap_AbstractCollectionTest {
	@Override
	protected def collectionUnderTest(NokeeMap subject) {
		return subject.entrySet()
	}

	@Override
	protected def byTypeFilter(Class type) {
		return { entry -> type.isAssignableFrom(entry.getValue().getType()) }
	}

	@Override
	protected configureValue(Action action) {
		return { entry -> entry.getValue().mapInPlace(configureInPlace(action))}
	}

	@Override
	protected asValues(Collection c) {
		return c.collect { entry -> entry.getValue().get() }
	}

	@Override
	protected callbackWithValue(Action action) {
		return { v -> action.execute(v.get().getValue()) }
	}

	def "can register for each action on entry set"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)

		and:
		def collection = subject.entrySet()

		when:
		def capturedKeys = []
		def capturedValues = []
		collection.forEach { entry ->
			capturedKeys << entry.key
			capturedValues << entry.value
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

	def "can get entries from entry set"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		and:
		def collection = subject.entrySet()

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)
		subject.put(k2, v2)

		when:
		subject.disallowChanges()
		def result = collection.get()

		then:
		result.collect { it.key } == [k1, k2]
		result.collect { it.value } == [v1, v2]
	}

	def "can resolve entries on provider from entry set"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		and:
		def collection = subject.entrySet()
		def provider = collection.elements

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)
		subject.put(k1, v1)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)
		subject.put(k2, v2)

		when:
		subject.disallowChanges()
		def result = provider.get()

		then:
		result.collect { it.key } == [k1, k2]
		result.collect { it.value } == [v1, v2]
	}

	def "can filter entry set by key"() {
		given:
		def subject = new NokeeMapImpl(String, objectFactory)

		and:
		def k1 = Stub(DomainObjectIdentifier)
		def v1 = Stub(Value)

		and:
		def k2 = Stub(DomainObjectIdentifier)
		def v2 = Stub(Value)

		and:
		def collection = subject.entrySet()
		def filteredCollection = collection.filter { it.key == k2 }

		and:
		subject.put(k1, v1)
		subject.put(k2, v2)

		when:
		subject.disallowChanges()
		def result = filteredCollection.get()

		then:
		result.collect { it.key } == [k2]
		result.collect { it.value } == [v2]
	}
}
