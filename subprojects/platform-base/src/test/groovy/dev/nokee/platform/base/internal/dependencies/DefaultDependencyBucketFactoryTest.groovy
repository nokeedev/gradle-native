package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.artifacts.Configuration
import spock.lang.Specification
import spock.lang.Subject

@Subject(DefaultDependencyBucketFactory)
class DefaultDependencyBucketFactoryTest extends Specification {
	def dependencyFactory = Mock(DependencyFactory)
	def configuration = Mock(Configuration)
	def configurationFactory = Mock(ConfigurationFactory)

	def "can create dependency bucket"() {
		given:
		def subject = new DefaultDependencyBucketFactory(configurationFactory, dependencyFactory)

		when:
		def bucket = subject.create('foo')

		then:
		bucket.name == 'foo'
		bucket.asConfiguration == configuration

		and:
		1 * configurationFactory.create('foo') >> configuration
		0 * _
	}
}
