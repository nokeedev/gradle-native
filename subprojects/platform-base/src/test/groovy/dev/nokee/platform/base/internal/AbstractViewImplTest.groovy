package dev.nokee.platform.base.internal

import com.google.common.collect.ImmutableSet
import dev.nokee.model.internal.NokeeCollection
import org.gradle.api.Action
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import spock.lang.Specification

import static dev.nokee.utils.ActionUtils.onlyIf
import static dev.nokee.utils.SpecUtils.byType

abstract class AbstractViewImplTest extends Specification {
	protected abstract def newSubject(NokeeCollection<?> collection)
	protected abstract Class<?> getViewImplementationType()

	def "can create"() {
		when:
		newSubject(Stub(NokeeCollection))

		then:
		noExceptionThrown()
	}

	def "throw exception when underlying collection is null"() {
		when:
		newSubject(null)

		then:
		thrown(NullPointerException)
	}

	def "delegates configureEach to underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)
		def action = Stub(Action)

		when:
		subject.configureEach(action)

		then:
		1 * collection.forEach(action)
		0 * collection._
	}

	def "delegates configureEach with type to underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)
		def action = Stub(Action)

		when:
		subject.configureEach(String, action)

		then:
		1 * collection.forEach(onlyIf(String, action))
		0 * collection._
	}

	def "delegates configureEach with spec to underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)
		def action = Stub(Action)
		def spec = { true } as Spec

		when:
		subject.configureEach(spec, action)

		then:
		1 * collection.forEach(onlyIf(spec, action))
		0 * collection._
	}

	def "creates new view from a filter by type of the underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)

		when:
		def result = subject.withType(String)

		then:
		1 * collection.filter(byType(String)) >> Stub(NokeeCollection)
		0 * collection._

		and:
		result != null
		viewImplementationType.isInstance(result)
	}

	def "filtered view wrap filtered collection"() {
		given:
		def filteredCollection = Mock(NokeeCollection)
		def collection = Mock(NokeeCollection) {
			filter(_) >> filteredCollection
		}
		def subject = newSubject(collection).withType(String)

		and:
		def action = Stub(Action)
		def spec = Stub(Spec)

		when:
		subject.configureEach(action)
		subject.configureEach(String, action)
		subject.configureEach(spec, action)
		subject.get()
		subject.withType(String)

		then:
		1 * filteredCollection.forEach(action)
		1 * filteredCollection.forEach(onlyIf(String, action))
		1 * filteredCollection.forEach(onlyIf(spec, action))
		1 * filteredCollection.get() >> []
		1 * filteredCollection.filter(byType(String)) >> Stub(NokeeCollection)
		0 * collection._
	}

	def "delegates get to the underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)

		when:
		def result = subject.get()

		then:
		1 * collection.get() >> []
		0 * collection._

		and:
		result != null
		result instanceof ImmutableSet
	}

	def "returns the values from the collection"() {
		given:
		def collection = Stub(NokeeCollection) {
			get() >> ['a', 'b', 'c']
		}
		def subject = newSubject(collection)

		expect:
		subject.get() == ['a', 'b', 'c'] as Set
	}

	def "returns an element provider without getting underlying collection"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)

		when:
		def result = subject.getElements()
		then:
		0 * collection.get()
		0 * collection._
		and:
		result != null
		result instanceof Provider

		when:
		result.get()
		then:
		noExceptionThrown()
		and:
		1 * collection.get() >> []
		0 * collection._
	}

	def "element provider returns the values from the collection"() {
		given:
		def collection = Stub(NokeeCollection) {
			get() >> ['a', 'b', 'c']
		}
		def subject = newSubject(collection)

		expect:
		subject.getElements().get() == ['a', 'b', 'c'] as Set
	}

	def "can map the element of the underlying collection lazily"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)

		when:
		def provider = subject.map { it + '-suffix' }
		then:
		0 * collection.get()
		0 * collection._
		and:
		provider != null

		when:
		def result = provider.get()
		then:
		1 * collection.get() >> ['a', 'b', 'c']
		0 * collection._
		and:
		result == ['a-suffix', 'b-suffix', 'c-suffix']
	}

	def "can flat map the element of the underlying collection lazily"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)

		when:
		def provider = subject.flatMap { [it + '1-suffix', it + '2-suffix'] }
		then:
		0 * collection.get()
		0 * collection._
		and:
		provider != null

		when:
		def result = provider.get()
		then:
		1 * collection.get() >> ['a', 'b', 'c']
		0 * collection._
		and:
		result == ['a1-suffix', 'a2-suffix', 'b1-suffix', 'b2-suffix', 'c1-suffix', 'c2-suffix']
	}

	def "can filter the element of the underlying collection lazily"() {
		given:
		def collection = Mock(NokeeCollection)
		def subject = newSubject(collection)

		when:
		def provider = subject.filter { it != 'b' }
		then:
		0 * collection.get()
		0 * collection._
		and:
		provider != null

		when:
		def result = provider.get()
		then:
		1 * collection.get() >> ['a', 'b', 'c']
		0 * collection._
		and:
		result == ['a', 'c']
	}
}
