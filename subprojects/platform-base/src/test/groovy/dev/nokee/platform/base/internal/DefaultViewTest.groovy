package dev.nokee.platform.base.internal

import dev.nokee.platform.base.DomainObjectCollection
import dev.nokee.platform.base.View
import org.gradle.api.Action
import org.gradle.api.Transformer
import org.gradle.api.provider.Provider
import org.gradle.api.specs.Spec
import spock.lang.Specification
import spock.lang.Subject

@Subject(DefaultView)
class DefaultViewTest extends Specification {
	def delegate = Mock(DomainObjectCollection)
	def subject = new DefaultView(delegate)

	def "forwards configureEach(Action) to delegate"() {
		given:
		def action = Mock(Action)

		when:
		subject.configureEach(action)

		then:
		1 * delegate.configureEach(action)
		0 * delegate._
	}

	def "forwards configureEach(Class, Action) to delegate"() {
		given:
		def type = String.class
		def action = Mock(Action)

		when:
		subject.configureEach(type, action)

		then:
		1 * delegate.configureEach(type, action)
		0 * delegate._
	}

	def "forwards configureEach(Spec, Action) to delegate"() {
		given:
		def spec = Mock(Spec)
		def action = Mock(Action)

		when:
		subject.configureEach(spec, action)

		then:
		1 * delegate.configureEach(spec, action)
		0 * delegate._
	}

	def "forwards withType(Class) to delegate"() {
		given:
		def type = String.class
		def subView = Mock(View)

		when:
		subject.withType(type)

		then:
		1 * delegate.withType(type) >> subView
		0 * delegate._
	}

	def "forwards getElements() to delegate"() {
		when:
		subject.getElements()

		then:
		1 * delegate.getElements() >> Mock(Provider)
		0 * delegate._
	}

	def "forwards get() to delegate"() {
		when:
		subject.get()

		then:
		1 * delegate.getElements() >> Mock(Provider) {
			get() >> []
		}
		0 * delegate._
	}

	def "forwards map(Transformer) to delegate"() {
		given:
		def transformer = Mock(Transformer)

		when:
		subject.map(transformer)

		then:
		1 * delegate.map(transformer)
		0 * delegate._
	}

	def "forwards flatMap(Transformer) to delegate"() {
		given:
		def transformer = Mock(Transformer)

		when:
		subject.flatMap(transformer)

		then:
		1 * delegate.flatMap(transformer)
		0 * delegate._
	}

	def "forwards filter(Spec) to delegate"() {
		given:
		def spec = Mock(Spec)

		when:
		subject.filter(spec)

		then:
		1 * delegate.filter(spec)
		0 * delegate._
	}
}
