package dev.nokee.model.internal.plugins

import dev.nokee.model.internal.DomainObjectEventPublisher
import dev.nokee.model.internal.RealizableDomainObjectRealizer
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Subject

@Subject(ModelBasePlugin)
class ModelBasePluginTest extends Specification {
	def project = ProjectBuilder.builder().build()

	def "registers event publisher service"() {
		when:
		project.apply plugin: ModelBasePlugin

		then:
		project.extensions.findByType(DomainObjectEventPublisher) != null
	}

	def "registers realizable service"() {
		when:
		project.apply plugin: ModelBasePlugin

		then:
		project.extensions.findByType(RealizableDomainObjectRealizer) != null
	}
}
