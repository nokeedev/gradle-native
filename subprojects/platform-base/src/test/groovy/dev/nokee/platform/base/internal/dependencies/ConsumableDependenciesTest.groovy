package dev.nokee.platform.base.internal.dependencies

import org.gradle.api.Action
import org.gradle.api.artifacts.ConfigurablePublishArtifact
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationPublications
import org.gradle.api.attributes.AttributeContainer
import spock.lang.Subject

@Subject(ConsumableDependencies)
class ConsumableDependenciesTest extends AbstractDependencyBucketTest<ConsumableDependencies> {
	@Override
	protected Class<ConsumableDependencies> getBucketType() {
		return ConsumableDependencies
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

	def "can access the underlying outgoing property from the configuration"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)
		def outgoing = Stub(ConfigurationPublications)

		when:
		def result = subject.getOutgoing()

		then:
		1 * configuration.getOutgoing() >> outgoing
		0 * configuration._

		and:
		result == outgoing
	}

	def "can configure the underlying outgoing property of the configuration"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)
		def action = Mock(Action)

		when:
		subject.outgoing(action)

		then:
		1 * configuration.outgoing(action)
		0 * configuration._
	}

	def "can add outgoing artifact"() {
		given:
		def outgoing = Mock(ConfigurationPublications)
		def configuration = Stub(Configuration) {
			getOutgoing() >> outgoing
		}
		def subject = newSubject(configuration)
		def notation = new Object()

		when:
		subject.artifact(notation)

		then:
		1 * outgoing.artifact(notation)
		0 * outgoing._
	}

	def "can add outgoing directory"() {
		given:
		def outgoing = Mock(ConfigurationPublications)
		def configuration = Stub(Configuration) {
			getOutgoing() >> outgoing
		}
		def subject = newSubject(configuration)
		def notation = new Object()
		def publishArtifact = Mock(ConfigurablePublishArtifact)

		when:
		subject.directory(notation)

		then:
		1 * outgoing.artifact(notation, _ as Action) >> { Object n, Action action -> action.execute(publishArtifact) }
		0 * outgoing._
		1 * publishArtifact.setType('directory')
		0 * publishArtifact._
	}
}
