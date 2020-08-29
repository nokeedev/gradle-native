package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.DependencySet
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import spock.lang.Subject

@Subject(DeclarableDependencies)
class DeclarableDependenciesTest extends AbstractDependencyBucketTest<DeclarableDependencies> {
	@Override
	protected Class<DeclarableDependencies> getBucketType() {
		return DeclarableDependencies
	}

	def "forwards created dependency from specified notation to underlying configuration"() {
		given:
		def dependencies = Mock(DependencySet)
		def configuration = Stub(Configuration) {
			getDependencies() >> dependencies
		}
		def dependency = Stub(ModuleDependency)
		def dependencyFactory = Mock(DependencyHandler)
		def subject = newSubject(identifier(), configuration, dependencyFactory)
		def notation = new Object()

		when:
		subject.addDependency(notation)

		then:
		1 * dependencyFactory.create(notation) >> dependency
		1 * dependencies.add(dependency)
		0 * dependencies._
	}

	def "forwards created dependency from specified notation with action to underlying configuration"() {
		given:
		def dependencies = Mock(DependencySet)
		def configuration = Stub(Configuration) {
			getDependencies() >> dependencies
		}
		def dependency = Stub(ModuleDependency)
		def dependencyFactory = Mock(DependencyHandler)
		def subject = newSubject(identifier(), configuration, dependencyFactory)
		def notation = new Object()
		def action = Mock(Action)

		when:
		subject.addDependency(notation, action)

		then:
		1 * dependencyFactory.create(notation) >> dependency
		1 * action.execute(dependency)

		then:
		1 * dependencies.add(dependency)
		0 * dependencies._
	}
}
