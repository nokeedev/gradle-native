package dev.nokee.utils

import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Shared
import spock.lang.Specification

import static dev.nokee.utils.ConfigureUtils.configureDisplayName

class ConfigureUtils_ConfigureDisplayNameTest extends Specification {
	@Shared def project = ProjectBuilder.builder().build()

	def "can configure display name of property"() {
		given:
		def property = project.objects.property(String)

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of set property"() {
		given:
		def property = project.objects.setProperty(String)

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of list property"() {
		given:
		def property = project.objects.listProperty(String)

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of regular file property"() {
		given:
		def property = project.objects.fileProperty()

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "can configure display name of directory property"() {
		given:
		def property = project.objects.directoryProperty()

		when:
		configureDisplayName(property, 'foo')

		then:
		property.declaredDisplayName.displayName == "property 'foo'"
	}

	def "returns the property"() {
		given:
		def property = project.objects.property(String)

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the set property"() {
		given:
		def property = project.objects.setProperty(String)

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the list property"() {
		given:
		def property = project.objects.listProperty(String)

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the regular file property"() {
		given:
		def property = project.objects.fileProperty()

		expect:
		configureDisplayName(property, 'foo') == property
	}

	def "returns the directory property"() {
		given:
		def property = project.objects.directoryProperty()

		expect:
		configureDisplayName(property, 'foo') == property
	}
}
