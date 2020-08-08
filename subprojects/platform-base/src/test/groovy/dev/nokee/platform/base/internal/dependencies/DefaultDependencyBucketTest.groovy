package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ModuleDependency
import spock.lang.Specification
import spock.lang.Subject

@Subject(DefaultDependencyBucket)
class DefaultDependencyBucketTest extends Specification {
	def dependency = Mock(ModuleDependency)
	def dependencyFactory = Mock(DependencyFactory)
	def dependencySet = Mock(DependencySet)
	def configuration = Mock(Configuration) {
		getDependencies() >> dependencySet
	}
	def subject = new DefaultDependencyBucket('foo', configuration, dependencyFactory)

	def "can add dependency"() {
		given:
		def notation = new Object()

		when:
		subject.addDependency(notation)
		then:
		1 * dependencyFactory.create(notation) >> dependency
		1 * dependencySet.add(dependency)

		when:
		subject.addDependency(notation, Mock(Action))
		then:
		1 * dependencyFactory.create(notation) >> dependency
		1 * dependencySet.add(dependency)
	}

	def "can configure dependency"() {
		given:
		def notation = new Object()
		def action = Mock(Action)

		and:
		dependencyFactory.create(_) >> dependency

		when:
		subject.addDependency(notation, action)

		then:
		1 * action.execute(dependency)
	}

	def "has name unrelated to configuration"() {
		when:
		def name = subject.name

		then:
		name == 'foo'

		and:
		0 * _ // name comes from bucket only
	}
}
