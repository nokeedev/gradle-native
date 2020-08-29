package dev.nokee.platform.base.internal.dependencies

import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification

class ComponentDependenciesContainerImplIntegrationTest extends Specification {
	def identifier = ProjectIdentifier.of('root')

	def "does not call configureEach action when configuration name is not known"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = new DependencyBucketInstantiatorImpl()
		instantiator.registerFactory(TestableBucket, new TestableBucketFactory())
		def subject = new ComponentDependenciesContainerImpl(identifier, project.configurations, instantiator)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		when:
		project.configurations.create('implementation')
		project.configurations.create('compileOnly')

		then:
		0 * action.execute(_)
	}

	def "calls configureEach action when configuration name is not known"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = new DependencyBucketInstantiatorImpl()
		instantiator.registerFactory(TestableBucket, new TestableBucketFactory(project.configurations))
		def subject = new ComponentDependenciesContainerImpl(identifier, project.configurations, instantiator)

		and:
		def implementationBucket = subject.register(DependencyBucketName.of('implementation'), TestableBucket)
		def compileOnlyBucket = subject.register(DependencyBucketName.of('compileOnly'), TestableBucket, Mock(Action))

		and:
		def action = Mock(Action)

		when:
		subject.configureEach(action)

		then:
		1 * action.execute(implementationBucket)
		1 * action.execute(compileOnlyBucket)
		0 * action.execute(_)
	}

	def "calls configureEach action registered before the bucket is known"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = new DependencyBucketInstantiatorImpl()
		instantiator.registerFactory(TestableBucket, new TestableBucketFactory(project.configurations))
		def subject = new ComponentDependenciesContainerImpl(identifier, project.configurations, instantiator)

		and:
		def action = Mock(Action)
		subject.configureEach(action)

		when:
		subject.register(DependencyBucketName.of('implementation'), TestableBucket)
		then:
		1 * action.execute({ it.name.get() == 'implementation' })
		0 * action.execute(_)

		when:
		subject.register(DependencyBucketName.of('compileOnly'), TestableBucket, Mock(Action))
		then:
		1 * action.execute({ it.name.get() == 'compileOnly' })
		0 * action.execute(_)
	}

	def "can find by bucket name known buckets"() {
		given:
		def project = ProjectBuilder.builder().build()
		def instantiator = new DependencyBucketInstantiatorImpl()
		instantiator.registerFactory(TestableBucket, new TestableBucketFactory(project.configurations))
		def subject = new ComponentDependenciesContainerImpl(identifier, project.configurations, instantiator)

		when:
		def implementationBucket = subject.register(DependencyBucketName.of('implementation'), TestableBucket)
		def compileOnlyBucket = subject.register(DependencyBucketName.of('compileOnly'), TestableBucket, Mock(Action))

		then:
		subject.findByName(DependencyBucketName.of('implementation')).present
		subject.findByName(DependencyBucketName.of('implementation')).get() == implementationBucket

		and:
		subject.findByName(DependencyBucketName.of('compileOnly')).present
		subject.findByName(DependencyBucketName.of('compileOnly')).get() == compileOnlyBucket
	}

	static class TestableBucket extends AbstractDependencyBucket {
		protected TestableBucket(DependencyIdentifier<? extends DependencyBucket> identifier, Configuration configuration) {
			super(identifier, configuration)
		}
	}
	static class TestableBucketFactory implements DependencyBucketFactory<TestableBucket> {
		private final ConfigurationContainer configurationContainer

		TestableBucketFactory(ConfigurationContainer configurationContainer) {
			this.configurationContainer = configurationContainer
		}

		@Override
		TestableBucket create(DependencyIdentifier<TestableBucket> identifier) {
			return new TestableBucket(identifier, configurationContainer.create(identifier.configurationName))
		}
	}
}
