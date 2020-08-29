package dev.nokee.platform.base.internal.dependencies

import dev.nokee.model.internal.DomainObjectIdentifierInternal
import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.internal.ComponentIdentifier
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ModuleDependency
import org.gradle.api.artifacts.dsl.DependencyHandler
import org.gradle.api.tasks.TaskDependency
import spock.lang.Specification
import spock.lang.Unroll

/**
 * General tests for all dependency bucket implementation.
 */
abstract class AbstractDependencyBucketTest<T extends DependencyBucket> extends Specification {
	protected T newInstance() {
		bucketType.newInstance()
	}

	protected T newSubject(DependencyIdentifier<T> identifier) {
		return newSubject(identifier, Stub(Configuration))
	}
	protected T newSubject(DependencyIdentifier<T> identifier, Configuration configuration) {
		def dependencyFactory = Stub(DependencyHandler) {
			create(_) >> Stub(ModuleDependency)
		}
		return DependencyBucketFactoryFactory.create(identifier, configuration, dependencyFactory, {newInstance()})
	}
	protected T newSubject(Configuration configuration) {
		return newSubject(identifier(), configuration)
	}

	protected T newSubject() {
		return newSubject(identifier(), Stub(Configuration))
	}

	protected T newSubject(DependencyIdentifier<T> identifier, Configuration configuration, DependencyHandler dependencyFactory) {
		return DependencyBucketFactoryFactory.create(identifier, configuration, dependencyFactory, {newInstance()})
	}

	protected abstract Class<T> getBucketType()

	protected DependencyIdentifier<T> identifier(String name = 'foo', DomainObjectIdentifierInternal owner = mainComponentOwner) {
		return DependencyIdentifier.of(DependencyBucketName.of(name), bucketType, owner)
	}

	protected static ComponentIdentifier getMainComponentOwner() {
		return ComponentIdentifier.ofMain(ProjectIdentifier.of('root'))
	}

	def "returns the bucket name from identifier"() {
		given:
		def subject = newSubject(identifier('foo'))

		expect:
		subject.getName() == DependencyBucketName.of('foo')
	}

	def "throws exception when extending a null bucket"() {
		given:
		def subject = newSubject()

		when:
		subject.extendsFrom(null)
		then:
		def ex = thrown(IllegalArgumentException)
		ex.message == 'Unable to extends from the specified buckets because argument #1 is null.'
	}

	def "throws exception when extending any null buckets"() {
		given:
		def subject = newSubject(identifier('foo'))

		when:
		subject.extendsFrom(null, newDeclarableBucket())
		then:
		def ex1 = thrown(IllegalArgumentException)
		ex1.message == 'Unable to extends from the specified buckets because argument #1 is null.'

		when:
		subject.extendsFrom(newDeclarableBucket(), null)
		then:
		def ex2 = thrown(IllegalArgumentException)
		ex2.message == 'Unable to extends from the specified buckets because argument #2 is null.'

		when:
		subject.extendsFrom(newDeclarableBucket(), newDeclarableBucket(), null)
		then:
		def ex3 = thrown(IllegalArgumentException)
		ex3.message == 'Unable to extends from the specified buckets because argument #3 is null.'
	}

	def "can specify no bucket to extends from"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)

		when:
		subject.extendsFrom()

		then:
		noExceptionThrown()

		and:
		0 * configuration.extendsFrom()
	}

	@Unroll
	def "forwards extendsFrom to underlying configuration"(bucket) {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)

		when:
		subject.extendsFrom(bucket)

		then:
		noExceptionThrown()

		and:
		1 * configuration.extendsFrom(bucket.configuration)

		where:
		bucket << [newDeclarableBucket('foo'), newConsumableBucket('foo'), newResolvableBucket('foo')]
	}

	@Unroll
	def "forwards extendsFrom to underlying configuration for custom buckets"(bucket) {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)

		when:
		subject.extendsFrom(bucket)

		then:
		noExceptionThrown()

		and:
		1 * configuration.extendsFrom(bucket.configuration)

		where:
		bucket << [newDeclarableBucket(), newConsumableBucket(), newResolvableBucket()]
	}

	static class MyDeclarableBucket extends BaseDeclarableDependencyBucket {}

	static class MyConsumableBucket extends BaseConsumableDependencyBucket {}

	static class MyResolvableBucket extends BaseResolvableDependencyBucket {}

	def "forwards extendsFrom to underlying configuration for any numbers of buckets"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(identifier('foo'), configuration)

		and:
		def bucket1 = newDeclarableBucket('bar')
		def bucket2 = newDeclarableBucket('far')

		when:
		subject.extendsFrom(bucket1, bucket2)

		then:
		noExceptionThrown()

		and:
		1 * configuration.extendsFrom(bucket1.configuration)
		1 * configuration.extendsFrom(bucket2.configuration)
	}

	def "is buildable"() {
		expect:
		org.gradle.api.Buildable.isAssignableFrom(bucketType)
	}

	def "returns buildable task dependency from the underlying configuration"() {
		given:
		def configuration = Mock(Configuration)
		def subject = newSubject(configuration)
		def taskDependency = Mock(TaskDependency)

		when:
		def result = subject.getBuildDependencies()

		then:
		1 * configuration.getBuildDependencies() >> taskDependency

		and:
		result == taskDependency
	}

	private MyDeclarableBucket newDeclarableBucket(String bucketName = 'foo') {
		def configuration = Mock(Configuration)
		return DependencyBucketFactoryFactory.create(DependencyIdentifier.of(DependencyBucketName.of(bucketName), MyDeclarableBucket.class, mainComponentOwner), configuration, Stub(DependencyHandler), {new MyDeclarableBucket()})
	}

	private MyConsumableBucket newConsumableBucket(String bucketName = 'foo') {
		def configuration = Mock(Configuration)
		return DependencyBucketFactoryFactory.create(DependencyIdentifier.of(DependencyBucketName.of(bucketName), MyConsumableBucket.class, mainComponentOwner), configuration, Stub(DependencyHandler),{new MyConsumableBucket()})
	}

	private MyResolvableBucket newResolvableBucket(String bucketName = 'foo') {
		def configuration = Mock(Configuration)
		return DependencyBucketFactoryFactory.create(DependencyIdentifier.of(DependencyBucketName.of(bucketName), MyResolvableBucket.class, mainComponentOwner), configuration, Stub(DependencyHandler),{new MyResolvableBucket()})
	}
}
