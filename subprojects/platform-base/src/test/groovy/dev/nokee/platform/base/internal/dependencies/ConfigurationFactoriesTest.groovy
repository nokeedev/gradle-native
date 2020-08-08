package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.artifacts.ConfigurationContainer
import spock.lang.Specification
import spock.lang.Subject

abstract class AbstractConfigurationFactorySpec extends Specification {}

@Subject(ConfigurationFactories.Prefixing)
class PrefixingConfigurationFactoryTest extends AbstractConfigurationFactorySpec {
	def factory = Mock(ConfigurationFactory)
	def prefixer = Mock(PrefixingNamingScheme)
	def subject = new ConfigurationFactories.Prefixing(factory, prefixer)

	def "can prefix name before creating configuration"() {
		when:
		subject.create('foo')

		then:
		1 * prefixer.prefix('foo') >> 'testFoo'
		1 * factory.create('testFoo')
		0 * _
	}
}

@Subject(ConfigurationFactories.Creating)
class CreatingConfigurationFactoryTest extends AbstractConfigurationFactorySpec {
	def configurations = Mock(ConfigurationContainer)
	def subject = new ConfigurationFactories.Creating(configurations)

	def "forwards creation to ConfigurationContainer"() {
		when:
		subject.create('foo')

		then:
		1 * configurations.create('foo')
		0 * _
	}
}

@Subject(ConfigurationFactories.MaybeCreating)
class MaybeCreatingConfigurationFactoryTest extends AbstractConfigurationFactorySpec {
	def configurations = Mock(ConfigurationContainer)
	def subject = new ConfigurationFactories.MaybeCreating(configurations)

	def "forwards creation to ConfigurationContainer"() {
		when:
		subject.create('foo')

		then:
		1 * configurations.maybeCreate('foo')
		0 * _
	}
}
