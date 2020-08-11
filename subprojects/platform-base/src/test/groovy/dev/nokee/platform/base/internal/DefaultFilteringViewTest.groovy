package dev.nokee.platform.base.internal

import groovy.transform.ToString
import org.gradle.api.Action
import org.gradle.api.specs.Spec
import spock.lang.Specification

class DefaultFilteringViewTest extends Specification {
	def a = new A(name: 'foo')
	def b = new B(name: 'bar')
	def filter = Mock(Spec)
	def subject = new DefaultFilteringView<A>(new TestableView<A>(a, b), filter)

	def "can configureEach filtered element"() {
		given:
		def action = Mock(Action)

		when:
		subject.configureEach(action)

		then:
		1 * filter.isSatisfiedBy(a) >> true
		1 * filter.isSatisfiedBy(b) >> false
		1 * action.execute(a)
		0 * action.execute(b)
	}

	def "can configureEach filtered element by type"() {
		given:
		def action = Mock(Action)

		when:
		subject.configureEach(B, action)

		then:
		1 * filter.isSatisfiedBy(a) >> true
		1 * filter.isSatisfiedBy(b) >> true
		0 * action.execute(a)
		1 * action.execute(b)
	}

	def "can configureEach filtered element using spec"() {
		given:
		def action = Mock(Action)
		def spec = Mock(Spec)

		when:
		subject.configureEach(spec, action)

		then:
		1 * filter.isSatisfiedBy(a) >> false
		1 * filter.isSatisfiedBy(b) >> true

		and:
		0 * spec.isSatisfiedBy(a)
		1 * spec.isSatisfiedBy(b) >> true

		and:
		0 * action.execute(a)
		1 * action.execute(b)
	}

	def "can realize filtered elements"() {
		when:
		def result = subject.get()

		then:
		1 * filter.isSatisfiedBy(a) >> false
		1 * filter.isSatisfiedBy(b) >> true

		and:
		result == [b] as Set
	}

	def "can get filtered elements"() {
		when:
		def result = subject.getElements().get()

		then:
		1 * filter.isSatisfiedBy(a) >> false
		1 * filter.isSatisfiedBy(b) >> true

		and:
		result == [b] as Set
	}

	def "can map filtered elements"() {
		when:
		def result = subject.map { it.name }.get()

		then:
		1 * filter.isSatisfiedBy(a) >> false
		1 * filter.isSatisfiedBy(b) >> true

		and:
		result == ['bar']
	}

	def "can flat map filtered elements"() {
		when:
		def result = subject.flatMap { ["${it.name}1", "${it.name}2"] }.get()

		then:
		1 * filter.isSatisfiedBy(a) >> false
		1 * filter.isSatisfiedBy(b) >> true

		and:
		result == ['bar1', 'bar2']
	}

	def "can filter filtered elements"() {
		given:
		def spec = Mock(Spec)

		when:
		def result = subject.filter(spec).get()

		then:
		1 * filter.isSatisfiedBy(a) >> false
		1 * filter.isSatisfiedBy(b) >> true

		and:
		0 * spec.isSatisfiedBy(a)
		1 * spec.isSatisfiedBy(b) >> true

		and:
		result == [b]
	}

	def "can create sub-type view"() {
		when:
		def result = subject.withType(B)
		then:
		result instanceof DefaultFilteringView

		when:
		def elements = result.get()
		then:
		1 * filter.isSatisfiedBy(a) >> true
		1 * filter.isSatisfiedBy(b) >> true
		and:
		elements == [b] as Set
	}

	@ToString
	static class A {
		String name
	}

	@ToString
	static class B extends A {}
}
