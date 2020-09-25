package dev.nokee.utils

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static dev.nokee.utils.ConfigureUtils.setPropertyValue

class ConfigureUtils_SetPropertyValueTest extends Specification {
	@Shared def project = ProjectBuilder.builder().build()

	def "can configure property"() {
		given:
		def property = project.objects.property(String)

		when:
		setPropertyValue(property, 'foo')
		then:
		property.get() == 'foo'

		when:
		setPropertyValue(property, project.provider { 'bar' })
		then:
		property.get() == 'bar'
	}

	def "can configure set property"() {
		given:
		def property = project.objects.setProperty(String)

		when:
		setPropertyValue(property, ['a', 'b', 'c'] as Set)
		then:
		property.get() == ['a', 'b', 'c'] as Set

		when:
		setPropertyValue(property, project.provider { ['x', 'y', 'z'] as Set })
		then:
		property.get() == ['x', 'y', 'z'] as Set
	}

	def "can configure list property"() {
		given:
		def property = project.objects.listProperty(String)

		when:
		setPropertyValue(property, ['a', 'b', 'c'])
		then:
		property.get() == ['a', 'b', 'c']

		when:
		setPropertyValue(property, project.provider { ['x', 'y', 'z'] })
		then:
		property.get() == ['x', 'y', 'z']
	}
}
