package dev.nokee.utils

import org.gradle.api.artifacts.Configuration
import spock.lang.Specification
import spock.lang.Subject

import static dev.nokee.utils.ConfigurationUtils.configureDescription

@Subject(ConfigurationUtils)
class ConfigurationUtils_ConfigureDescriptionTest extends Specification {
	def "can configure configuration description"() {
		given:
		def configuration = Mock(Configuration)

		when:
		configureDescription({ 'some description' }).execute(configuration)

		then:
		1 * configuration.setDescription('some description')
	}

	def "throws exception if supplier is null"() {
		when:
		configureDescription(null)

		then:
		thrown(NullPointerException)
	}

	def "can compare configuration action"() {
		given:
		def supplier = { 'description' }

		expect:
		configureDescription(supplier) == configureDescription(supplier)
		configureDescription(supplier) != configureDescription({ 'another description' })
	}

	def "action toString() explains where it comes from"() {
		given:
		def supplier = { 'description' }

		expect:
		configureDescription(supplier).toString() == "ConfigurationUtils.configureDescription(${supplier.toString()})"
	}
}
