package dev.nokee.platform.base

import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal
import org.gradle.api.Action
import spock.lang.Specification

abstract class AbstractComponentDependenciesDelegateTest extends Specification {
	final ComponentDependenciesInternal delegate = Mock()

	protected ComponentDependenciesInternal getDelegate() {
		return delegate
	}

	protected abstract BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate)

	def "forwards add(String, Object) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = 'foo'
		def notation = new Object()

		when:
		subject.add(bucketName, notation)

		then:
		1 * delegate.add(bucketName, notation)
		0 * _
	}

	def "forwards add(String, Object, Action) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = 'foo'
		def notation = new Object()
		def action = Mock(Action)

		when:
		subject.add(bucketName, notation, action)

		then:
		1 * delegate.add(bucketName, notation, action)
		0 * _
	}

	def "forwards create(String) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = 'foo'

		when:
		subject.create(bucketName)

		then:
		1 * delegate.create(bucketName)
		0 * _
	}

	def "forwards create(String, Action) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = 'foo'
		def action = Mock(Action)

		when:
		subject.create(bucketName, action)

		then:
		1 * delegate.create(bucketName, action)
		0 * _
	}

	def "forwards configureEach(Action) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def action = Mock(Action)

		when:
		subject.configureEach(action)

		then:
		1 * delegate.configureEach(action)
		0 * _
	}

	def "forwards findByName(String) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = 'foo'

		when:
		subject.findByName(bucketName)

		then:
		1 * delegate.findByName(bucketName)
		0 * _
	}

	def "forwards getByName(String) method to delegate"() {
		given:
		def subject = newSubject(delegate)
		def bucketName = 'foo'

		when:
		subject.getByName(bucketName)

		then:
		1 * delegate.getByName(bucketName)
		0 * _
	}

}
