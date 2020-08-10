package dev.nokee.platform.base.internal

import dev.nokee.platform.base.DomainObjectElement
import org.gradle.api.Action
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

import java.util.function.Supplier

@Subject(DefaultDomainObjectCollection)
class DefaultDomainObjectCollectionTest extends Specification {
	def project = ProjectBuilder.builder().build()
	def subject = new DefaultDomainObjectCollection<String>(String.class, project.objects, project.providers)

	def "can add element to collection"() {
		when:
		subject.add(DomainObjectElement.of('foo'))

		then:
		noExceptionThrown()

		and:
		subject.size() == 1
	}

	def "reports empty when no element in collection"() {
		expect:
		subject.size() == 0
	}

	def "can add future element to collection"() {
		when:
		subject.add(DomainObjectElement.of(String, new Supplier<String>() {
			@Override
			String get() {
				return 'foo'
			}
		}))

		then:
		noExceptionThrown()

		and:
		subject.size() == 1
	}

	def "dispatches whenElementKnown event on every element added"() {
		given:
		def action = Mock(Action)
		subject.whenElementKnown(action)

		when:
		subject.add(DomainObjectElement.of('foo'))
		then:
		1 * action.execute(_)

		when:
		subject.add(DomainObjectElement.of(String, new Supplier<String>() {
			@Override
			String get() {
				return 'foo'
			}
		}))
		then:
		1 * action.execute(_)
	}

	def "dispatches configureEach event only when element is realized"() {
		given:
		def action = Mock(Action)
		subject.configureEach(action)

		when:
		subject.add(DomainObjectElement.of('foo'))
		then:
		1 * action.execute(_)

		when:
		subject.add(DomainObjectElement.of(String, new Supplier<String>() {
			@Override
			String get() {
				return 'foo'
			}
		}))
		then:
		0 * action.execute(_)
	}
}
