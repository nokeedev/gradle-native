package dev.nokee.platform.base.internal.dependencies

import dev.nokee.platform.base.ConsumableDependencyBucket
import dev.nokee.platform.base.DeclarableDependencyBucket
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.ResolvableDependencyBucket
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ConfigurationContainer
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.testfixtures.ProjectBuilder
import spock.lang.Specification
import spock.lang.Unroll

import javax.inject.Provider

class DependencyBucketFactoryFactoryTest<T extends DependencyBucketFactory> extends Specification {
	private DependencyBucketFactoryFactory newSubject() {
		return new DependencyBucketFactoryFactory(Stub(ConfigurationContainer), Stub(DependencyHandler))
	}

	private DependencyBucketFactoryFactory newSubject(ConfigurationContainer configurations) {
		return new DependencyBucketFactoryFactory(configurations, Stub(DependencyHandler))
	}

	private DependencyBucketFactoryFactory newSubject(ConfigurationContainer configurations, DependencyHandler dependencies) {
		return new DependencyBucketFactoryFactory(configurations, dependencies)
	}

	protected <T extends DependencyBucket> DependencyIdentifier<T> identifier(String name, Class<T> type = DeclarableDependencyBucket) {
		return DependencyIdentifier.of(DependencyBucketName.of(name), type, ProjectIdentifier.of('root'))
	}

	def "can wrap a injectable provider into a dependency bucket factory"() {
		given:
		def subject = newSubject()
		def bucketProvider = Mock(Provider)

		when:
		def factory = subject.wrap(bucketProvider)

		then:
		0 * bucketProvider._
		factory instanceof DependencyBucketFactory
	}

	def "creates missing configuration"() {
		given:
		def configurations = Mock(ConfigurationContainer)
		def subject = newSubject(configurations)
		def bucketProvider = Mock(Provider)
		def factory = subject.wrap(bucketProvider)
		def bucket = Stub(BaseDeclarableDependencyBucket)

		when:
		factory.create(identifier('foo', DeclarableDependencyBucket))

		then:
		1 * configurations.findByName('foo') >> null // "missing configuration"
		1 * configurations.create('foo') >> Mock(Configuration)
		1 * bucketProvider.get() >> bucket
	}

	def "can use existing configuration"() {
		given:
		def configurations = Mock(ConfigurationContainer)
		def subject = newSubject(configurations)
		def bucketProvider = Mock(Provider)
		def factory = subject.wrap(bucketProvider)
		def bucket = Stub(BaseDeclarableDependencyBucket)
		def configuration = Stub(Configuration) {
			isCanBeConsumed() >> false
			isCanBeResolved() >> false
		}

		when:
		factory.create(identifier('foo', DeclarableDependencyBucket))

		then:
		1 * configurations.findByName('foo') >> configuration
		0 * configurations.create('foo')
		1 * bucketProvider.get() >> bucket
	}

	@Unroll
	def "throws exception if existing configuration is not configured properly"(bucketType, bucketRole, expectedCanBeConsumed, expectedCanBeResolved) {
		given:
		def configuration = Stub(Configuration) {
			getName() >> 'foo'
			isCanBeConsumed() >> true
			isCanBeResolved() >> true
		}
		def configurations = Stub(ConfigurationContainer) {
			findByName(_) >> configuration
		}
		def factory = newSubject(configurations).wrap(Mock(Provider))

		when:
		factory.create(identifier('foo', bucketType))

		then:
		def ex = thrown(IllegalStateException)
		ex.message == "Cannot reuse existing configuration named 'foo' as a ${bucketRole} bucket of dependencies because it does not match the expected configuration (expecting: [canBeConsumed: ${expectedCanBeConsumed}, canBeResolved: ${expectedCanBeResolved}], actual: [canBeConsumed: true, canBeResolved: true])."

		where:
		bucketType 					| bucketRole	| expectedCanBeConsumed | expectedCanBeResolved
		DeclarableDependencyBucket 	| 'declarable'	| false 				| false
		ConsumableDependencyBucket 	| 'consumable'	| true 					| false
		ResolvableDependencyBucket 	| 'resolvable'	| false 				| true
	}

	@Unroll
	def "configures new configuration to expected bucket configuration"(bucketType, expectedCanBeConsumed, expectedCanBeResolved, expectedDescription) {
		given:
		def configuration = Mock(Configuration)
		def configurations = Stub(ConfigurationContainer) {
			create(_) >> configuration
			findByName(_) >> null
		}
		def factory = newSubject(configurations).wrap(Mock(Provider))

		when:
		factory.create(identifier('foo', bucketType))

		then:
		1 * configuration.setCanBeResolved(expectedCanBeResolved)
		1 * configuration.setCanBeConsumed(expectedCanBeConsumed)
		1 * configuration.setDescription(expectedDescription)
		0 * configuration._

		where:
		bucketType 					| expectedCanBeConsumed | expectedCanBeResolved | expectedDescription
		DeclarableDependencyBucket 	| false 				| false					| "Foo dependencies for project 'root'."
		ConsumableDependencyBucket 	| true 					| false					| "Foo for project 'root'."
		ResolvableDependencyBucket 	| false 				| true					| "Foo for project 'root'."
	}

	def "next dependency bucket information is empty"() {
		expect:
		DependencyBucketFactoryFactory.NEXT_DEPENDENCY_BUCKET_INFO.get() == null
	}

	def "next dependency bucket information is empty after bucket creation"() {
		given:
		def configuration = Mock(Configuration)
		def configurations = Stub(ConfigurationContainer) {
			create(_) >> configuration
			findByName(_) >> null
		}
		def factory = newSubject(configurations).wrap(Mock(Provider))

		when:
		factory.create(identifier('foo', DeclarableDependencyBucket))

		then:
		DependencyBucketFactoryFactory.NEXT_DEPENDENCY_BUCKET_INFO.get() == null
	}

	@Unroll
	def "next dependency bucket information contains bucket under creation when provider is called"(bucketType) {
		given:
		def configuration = Stub(Configuration)
		def configurations = Stub(ConfigurationContainer) {
			create(_) >> configuration
		}
		def dependencies = Stub(DependencyHandler)
		def bucketUnderCreationInfo = null
		def bucketProvider = {
			bucketUnderCreationInfo = DependencyBucketFactoryFactory.NEXT_DEPENDENCY_BUCKET_INFO.get()
			return Stub(bucketType)
		}
		def factory = newSubject(configurations, dependencies).wrap(bucketProvider)
		def identifier = identifier('foo', bucketType)

		when:
		factory.create(identifier)
		then:
		configurations.findByName(_) >> configuration
		configuration.canBeConsumed >> ConsumableDependencyBucket.isAssignableFrom(bucketType)
		configuration.canBeResolved >> ResolvableDependencyBucket.isAssignableFrom(bucketType)
		bucketUnderCreationInfo != null
		bucketUnderCreationInfo.identifier == identifier
		bucketUnderCreationInfo.configuration == configuration
		bucketUnderCreationInfo.dependencies == dependencies

		when:
		factory.create(identifier)
		then:
		configurations.findByName(_) >> null
		bucketUnderCreationInfo != null
		bucketUnderCreationInfo.identifier == identifier
		bucketUnderCreationInfo.configuration == configuration
		bucketUnderCreationInfo.dependencies == dependencies

		where:
		bucketType << [DeclarableDependencyBucket, ConsumableDependencyBucket, ResolvableDependencyBucket]
	}

	@Unroll
	def "can create generic buckets"(bucketType) {
		given:
		def project = ProjectBuilder.builder().build()
		def factory = newSubject(project.configurations).wrap({ bucketType.newInstance() })

		when:
		def result = factory.create(identifier('foo', bucketType))

		then:
		bucketType.isAssignableFrom(result.class)
		result.configuration == project.configurations.foo

		where:
		bucketType << [DeclarableDependencies, ResolvableDependencies, ConsumableDependencies]
	}

	@Unroll
	def "throw exception if buckets are instantiated directly"(bucketType) {
		when:
		bucketType.newInstance()

		then:
		def ex = thrown(RuntimeException)
		ex.message == 'Direct instantiation of a Base*DependencyBucket is not permitted.'

		where:
		bucketType << [DeclarableDependencies, ResolvableDependencies, ConsumableDependencies]
	}
}
