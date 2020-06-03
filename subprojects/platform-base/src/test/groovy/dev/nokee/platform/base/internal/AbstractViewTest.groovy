package dev.nokee.platform.base.internal

import dev.nokee.platform.base.Binary
import dev.nokee.platform.base.Variant
import org.gradle.api.Task
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

abstract class AbstractViewTest<T> extends Specification {
	def project = ProjectBuilder.builder().build()
	def objects = project.objects
	def providers = project.providers
	def realizeTrigger = Mock(Realizable)

	abstract def getBackingCollection()
	abstract void realizeBackingCollection()
	abstract def createView()

	abstract Provider<T> getA()
	abstract Provider<T> getB()
	abstract <S extends T> Provider<S> getC()

	abstract Class<T> getType()

	abstract <S extends T> Class<S> getOtherType()

	abstract void addToBackingCollection(Provider<T> v)

	protected interface Identifiable {
		String getIdentification()
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
		realizeBackingCollection()
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
		realizeBackingCollection()
		then:
		configuredElements*.identification == ['b']
		backingCollection*.identification == ['a', 'b']
	}

	def "can configure each elements according to a type"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)
		addToBackingCollection(c)

		when:
		def configuredElements = []
		view.configureEach((Class)otherType) { configuredElements << it }
		then:
		configuredElements == []

		when:
		realizeBackingCollection()
		then:
		configuredElements*.identification == ['c']
		backingCollection*.identification == ['a', 'b', 'c']
	}

	def "can configure each elements of a filtered view by type"() {
		given:
		def view = createView()

		and:
		addToBackingCollection(a)
		addToBackingCollection(b)
		addToBackingCollection(c)

		when:
		def configuredElements = []
		view.withType((Class)otherType).configureEach(otherType) { configuredElements << it }
		then:
		configuredElements == []

		when:
		realizeBackingCollection()
		then:
		configuredElements*.identification == ['c']
		backingCollection*.identification == ['a', 'b', 'c']
	}

	def "throws an exception when configureEach action is null"() {
		given:
		def view = createView()

		when:
		view.configureEach(null)
		then:
		def ex1 = thrown(IllegalArgumentException)
		ex1.message == "configure each action for ${displayName} must not be null"

		when:
		view.configureEach(type, null)
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == "configure each action for ${displayName} must not be null"

		when:
		view.configureEach({ type.isAssignableFrom(it.class) }, null)
		then:
		def ex3 = thrown(IllegalArgumentException)
		ex3.message == "configure each action for ${displayName} must not be null"
	}

	def "throws an exception when filtering view by type with null"() {
		given:
		def view = createView()

		when:
		view.withType(null)
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "${displayName} subview type must not be null"
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

	def "throws an exception when mapping with a null transformer"() {
		given:
		def view = createView()

		when:
		view.map(null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "map mapper for ${displayName} must not be null"
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

	def "throws an exception when flat mapping with a null transformer"() {
		given:
		def view = createView()

		when:
		view.flatMap(null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "flatMap mapper for ${displayName} must not be null"
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

	def "throws an exception when filtering with a null spec"() {
		given:
		def view = createView()

		when:
		view.filter(null)

		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == "filter spec for ${displayName} must not be null"
	}

	private String getDisplayName() {
		if (Binary.isAssignableFrom(type)) {
			return 'binary view'
		} else if (Variant.isAssignableFrom(type)) {
			return 'variant view'
		} else if (Task.isAssignableFrom(type)) {
			return 'task view'
		}
		throw new IllegalArgumentException('View type not recognized')
	}
}
