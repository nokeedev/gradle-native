package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.attributes.AttributeContainer
import org.gradle.api.file.FileCollection
import spock.lang.Subject

@Subject(ResolvableDependencies)
class ResolvableDependenciesTest extends AbstractDependencyBucketTest<ResolvableDependencies> {
	@Override
	protected Class<ResolvableDependencies> getBucketType() {
		return ResolvableDependencies
	}

	def "can access the underlying incoming configuration property"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)
		def incoming = Stub(org.gradle.api.artifacts.ResolvableDependencies)

		when:
		def result = subject.getIncoming()

		then:
		1 * configuration.getIncoming() >> incoming
		0 * configuration._

		and:
		result == incoming
	}

	def "can access the underlying attributes from the configuration"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)
		def attributes = Stub(AttributeContainer)

		when:
		def result = subject.getAttributes()

		then:
		1 * configuration.getAttributes() >> attributes
		0 * configuration._

		and:
		result == attributes
	}

	def "can configure the underlying attributes of the configuration"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)
		def action = Mock(Action)

		when:
		subject.attributes(action)

		then:
		1 * configuration.attributes(action)
		0 * configuration._
	}

	def "can returns the incoming files of the underlying configuration"() {
		given:
		def incomingFiles = Stub(FileCollection)
		def incoming = Stub(org.gradle.api.artifacts.ResolvableDependencies) {
			getFiles() >> incomingFiles
		}
		def configuration = Stub(Configuration) {
			getIncoming() >> incoming
		}
		def subject = newSubject(configuration)

		expect:
		subject.getFiles() == incomingFiles
	}
}
