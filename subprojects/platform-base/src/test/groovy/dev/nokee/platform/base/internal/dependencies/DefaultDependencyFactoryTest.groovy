package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.artifacts.dsl.DependencyHandler
import spock.lang.Specification
import spock.lang.Subject

@Subject(DefaultDependencyFactory)
class DefaultDependencyFactoryTest extends Specification {
	def dependencies = Mock(DependencyHandler)
	def subject = new DefaultDependencyFactory(dependencies)

	def "forwards dependency creation to DependencyHandler"() {
		given:
		def notation = new Object()

		when:
		subject.create(notation)

		then:
		1 * dependencies.create(notation)
		0 * _
	}
}
