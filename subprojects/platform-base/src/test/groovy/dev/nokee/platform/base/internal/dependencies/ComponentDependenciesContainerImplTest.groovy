package dev.nokee.platform.base.internal.dependencies

import dev.nokee.platform.base.DeclarableDependencyBucket
import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import dev.nokee.platform.base.internal.VariantIdentifier
import org.gradle.api.Action
import org.gradle.api.artifacts.ConfigurationContainer
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ComponentDependenciesContainerImpl)
class ComponentDependenciesContainerImplTest extends Specification {
	def identifier = ProjectIdentifier.of('root')

	def "can register new bucket"() {
		given:
		def instantiator = Mock(DependencyBucketInstantiator)
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		def bucket = Mock(TestableBucket)

		when:
		def result = subject.register(DependencyBucketName.of('implementation'), TestableBucket)

		then:
		1 * instantiator.newInstance(_, _) >> bucket
		result == bucket
	}

	def "invokes instantiator with the registering type"() {
		given:
		def instantiator = Mock(DependencyBucketInstantiator)
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		def bucket = Mock(TestableBucket)

		when:
		subject.register(DependencyBucketName.of('implementation'), TestableBucket)

		then:
		1 * instantiator.newInstance(_, TestableBucket) >> bucket
	}

	@Unroll
	def "invokes instantiator with child identifier"(identifier) {
		given:
		def instantiator = Mock(DependencyBucketInstantiator)
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		def bucket = Mock(TestableBucket)
		def bucketName = DependencyBucketName.of('implementation')

		when:
		subject.register(bucketName, TestableBucket)

		then:
		1 * instantiator.newInstance(DependencyIdentifier.of(bucketName, TestableBucket, identifier), _) >> bucket

		where:
		identifier << [ProjectIdentifier.of('root'), ComponentIdentifier.ofMain(ProjectIdentifier.of('root')), VariantIdentifier.of('macosDebug', ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))]
	}

	def "can register new bucket with configuration action"() {
		given:
		def instantiator = Mock(DependencyBucketInstantiator)
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		def bucket = Mock(TestableBucket)
		def action = Mock(Action)

		when:
		def result = subject.register(DependencyBucketName.of('implementation'), TestableBucket, action)

		then:
		1 * instantiator.newInstance(_, _) >> bucket
		1 * action.execute(bucket)
		result == bucket
	}

	def "invokes instantiator with the registering type with configuration action"() {
		given:
		def instantiator = Mock(DependencyBucketInstantiator)
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		def bucket = Mock(TestableBucket)
		def action = Mock(Action)

		when:
		subject.register(DependencyBucketName.of('implementation'), TestableBucket, action)

		then:
		1 * instantiator.newInstance(_, TestableBucket) >> bucket
		1 * action.execute(bucket)
	}

	@Unroll
	def "invokes instantiator with child identifier with configuration action"(identifier) {
		given:
		def instantiator = Mock(DependencyBucketInstantiator)
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		def bucket = Mock(TestableBucket)
		def bucketName = DependencyBucketName.of('implementation')
		def action = Mock(Action)

		when:
		subject.register(bucketName, TestableBucket, action)

		then:
		1 * instantiator.newInstance(DependencyIdentifier.of(bucketName, TestableBucket, identifier), _) >> bucket
		1 * action.execute(bucket)

		where:
		identifier << [ProjectIdentifier.of('root'), ComponentIdentifier.ofMain(ProjectIdentifier.of('root')), VariantIdentifier.of('macosDebug', ComponentIdentifier.ofMain(ProjectIdentifier.of('root')))]
	}

	def "returns empty optional for unknown buckets"() {
		given:
		def bucket = Mock(TestableBucket)
		def instantiator = Mock(DependencyBucketInstantiator) {
			newInstance(_, _) >> bucket
		}

		when:
		def subject = new ComponentDependenciesContainerImpl(identifier, Mock(ConfigurationContainer), instantiator)
		then:
		!subject.findByName(DependencyBucketName.of('linkOnly')).present

		when:
		subject.register(DependencyBucketName.of('implementation'), TestableBucket)
		subject.register(DependencyBucketName.of('compileOnly'), TestableBucket, Mock(Action))
		then:
		!subject.findByName(DependencyBucketName.of('linkOnly')).present
	}

	interface TestableBucket extends DeclarableDependencyBucket {}
}
