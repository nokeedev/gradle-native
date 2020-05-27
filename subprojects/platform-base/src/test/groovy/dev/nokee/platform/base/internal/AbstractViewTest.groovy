package dev.nokee.platform.base.internal

import org.gradle.api.DomainObjectCollection
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class AbstractViewTest<T> extends Specification {
	def project = ProjectBuilder.builder().build()
	def objects = project.objects
	def providers = project.providers
	def realizeTrigger = Mock(Realizable)

	abstract def getBackingCollection()
	def createView() {
		return objects.newInstance(viewType, backingCollection, realizeTrigger)
	}
	abstract Class getViewType()

	abstract Provider<T> getA()
	abstract Provider<T> getB()

	abstract void addToBackingCollection(Provider<T> v)

	protected interface Identifiable {
		String getIdentification()
	}

	void resolve(DomainObjectCollection collection) {
		collection.iterator().next()
	}

	def "can configure each elements"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def configuredElements = []
		view.configureEach { configuredElements << it }
		then:
		configuredElements == []

		when:
		resolve(backingCollection)
		then:
		configuredElements*.identification == ['a', 'b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can configure each elements according to a specification"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def configuredElements = []
		view.configureEach({ it.identification == 'b' }) { configuredElements << it }
		then:
		configuredElements == []

		when:
		resolve(backingCollection)
		then:
		configuredElements*.identification == ['b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can get all the realized elements"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def elements = view.get()

		then:
		1 * realizeTrigger.realize()
		and:
		elements*.identification == ['a', 'b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can get all the elements as a provider"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def elements = view.elements
		then:
		0 * realizeTrigger.realize()

		when:
		def allElements = elements.get()
		then:
		1 * realizeTrigger.realize()
		and:
		allElements*.identification == ['a', 'b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can map all the elements as a provider"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def elements = view.map { it.identification }
		then:
		0 * realizeTrigger.realize()

		when:
		def allElements = elements.get()
		then:
		1 * realizeTrigger.realize()
		and:
		allElements == ['a', 'b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can flatMap all the elements as a provider"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def elements = view.flatMap {
			if (it.identification == 'a') {
				return []
			}
			return [it.identification]
		}
		then:
		0 * realizeTrigger.realize()

		when:
		def allElements = elements.get()
		then:
		1 * realizeTrigger.realize()
		and:
		allElements == ['b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can filter all the elements as a provider"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)

		when:
		def elements = view.filter { it.identification == 'b' }
		then:
		0 * realizeTrigger.realize()

		when:
		def allElements = elements.get()
		then:
		1 * realizeTrigger.realize()
		and:
		allElements*.identification == ['b']
		backingCollection*.identification == ['a', 'b']
	}
}
