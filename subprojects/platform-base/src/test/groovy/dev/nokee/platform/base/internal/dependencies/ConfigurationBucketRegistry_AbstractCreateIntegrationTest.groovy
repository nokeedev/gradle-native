package dev.nokee.platform.base.internal.dependencies

import dev.nokee.internal.testing.utils.TestUtils
import org.gradle.api.Rule
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ConfigurationBucketRegistryImpl)
abstract class ConfigurationBucketRegistry_AbstractCreateIntegrationTest extends Specification {
	def project = TestUtils.rootProject()

	protected abstract def create(ConfigurationBucketRegistryImpl subject, String name, ConfigurationBucketType type)

	@Unroll
	def "can create missing configuration"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		when:
		def result = create(subject, 'foo', type)

		then:
		project.configurations.findByName('foo') != null

		and:
		result != null
		result.canBeResolved == type.canBeResolved
		result.canBeConsumed == type.canBeConsumed

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "return existing configuration without changing properties"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		def existingConfiguration = project.configurations.create('foo') {
			canBeResolved = type.canBeResolved
			canBeConsumed = type.canBeConsumed
		}

		when:
		def result = create(subject, 'foo', type)

		then:
		result == existingConfiguration
		result.canBeResolved == type.canBeResolved
		result.canBeConsumed == type.canBeConsumed

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "throw exception when existing configuration is not configured properly"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		project.configurations.create('foo') {
			canBeResolved = true
			canBeConsumed = true
		}

		when:
		create(subject, 'foo', type)

		then:
		def ex = thrown(IllegalStateException)
		ex.message == "Cannot reuse existing configuration named 'foo' as a ${type.bucketTypeName} bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: ${type.canBeConsumed}, canBeResolved: ${type.canBeResolved}], actual: [canBeConsumed: true, canBeResolved: true])."

		where:
		type << ConfigurationBucketType.values()
	}

	@Unroll
	def "does not trigger configuration container rules when configuration is absent"(type) {
		given:
		def subject = new ConfigurationBucketRegistryImpl(project.configurations)

		and:
		def rule = Mock(Rule)
		project.configurations.addRule(rule)

		when:
		create(subject, 'foo', type)

		then:
		0 * rule.apply('foo')

		where:
		type << ConfigurationBucketType.values()
	}
}
