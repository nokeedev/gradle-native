package dev.nokee.platform.base.internal.dependencies

import dev.nokee.platform.base.DependencyBucket
import dev.nokee.platform.base.DependencyBucketName
import dev.nokee.platform.base.internal.ProjectIdentifier
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import spock.lang.Specification
import spock.lang.Subject

@Subject(DependencyBucketCreationGuard)
class DependencyBucketCreationGuardTest extends Specification {
	def "does not forward to wrapped action for unknown configuration name"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def action = Mock(Action)
		def configuration = Mock(Configuration)
		def mapperAction = subject.mapToDependencyBucket(action)

		when:
		mapperAction.execute(configuration)
		then:
		configuration.name >> 'implementation'
		0 * action.execute(_)

		when:
		mapperAction.execute(configuration)
		then:
		configuration.name >> 'compileOnly'
		0 * action.execute(_)

		when:
		mapperAction.execute(configuration)
		then:
		configuration.name >> 'linkOnly'
		0 * action.execute(_)
	}

	def "forwards to wrapped action for known configuration name"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def action = Mock(Action)
		def configuration = Mock(Configuration)
		def mapperAction = subject.mapToDependencyBucket(action)
		def bucket = Mock(DependencyBucket)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		when:
		subject.guard(identifier, { bucket } as DependencyBucketFactory<TestableBucket>)
		mapperAction.execute(configuration)
		then:
		configuration.name >> identifier.configurationName
		1 * action.execute(bucket)

		when:
		mapperAction.execute(configuration)
		then:
		configuration.name >> "test${identifier.configurationName.capitalize()}"
		0 * action.execute(_)
	}

	def "returns the value from the factory"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		when:
		def result = subject.guard(identifier, { bucket } as DependencyBucketFactory<TestableBucket>)

		then:
		result == bucket
	}

	def "invokes the factory only once"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def factory = Mock(DependencyBucketFactory)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		when:
		subject.guard(identifier, factory)

		then:
		1 * factory.create(identifier) >> bucket
	}

	def "executes mapped action if called before factory returns"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		and:
		def action = Mock(Action)
		def mapperAction = subject.mapToDependencyBucket(action)

		and:
		def factory = {
			def configuration = Mock(Configuration) {
				getName() >> identifier.configurationName
			}
			mapperAction.execute(configuration)
			bucket
		} as DependencyBucketFactory<TestableBucket>

		when:
		subject.guard(identifier, factory)

		then:
		1 * action.execute(bucket)
	}

	def "executes multiple mapped action if called before factory returns"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		and:
		def action1 = Mock(Action)
		def action2 = Mock(Action)
		def action3 = Mock(Action)
		def mapperAction1 = subject.mapToDependencyBucket(action1)
		def mapperAction2 = subject.mapToDependencyBucket(action2)
		def mapperAction3 = subject.mapToDependencyBucket(action3)

		and:
		def factory = {
			def configuration = Mock(Configuration) {
				getName() >> identifier.configurationName
			}
			mapperAction1.execute(configuration)
			mapperAction2.execute(configuration)
			mapperAction3.execute(configuration)
			bucket
		} as DependencyBucketFactory<TestableBucket>

		when:
		subject.guard(identifier, factory)

		then:
		1 * action1.execute(bucket)
		and:
		1 * action2.execute(bucket)
		and:
		1 * action3.execute(bucket)
	}

	def "premature execution of mapped action does not affect subsequent tracking"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def identifier1 = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))
		def identifier2 = DependencyIdentifier.of(DependencyBucketName.of('compileOnly'), TestableBucket, ProjectIdentifier.of('root'))

		and:
		def action = Mock(Action)
		def mapperAction = subject.mapToDependencyBucket(action)

		and:
		def factory = {
			def configuration = Mock(Configuration) {
				getName() >> identifier1.configurationName
			}
			mapperAction.execute(configuration)
			bucket
		} as DependencyBucketFactory<TestableBucket>
		subject.guard(identifier1, factory)

		when:
		subject.guard(identifier2, { bucket } as DependencyBucketFactory<TestableBucket>)

		then:
		0 * action.execute(bucket)
	}

	def "a factory throwing during premature execution of mapped action does not affect subsequent tracking"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		and:
		def action = Mock(Action)
		def mapperAction = subject.mapToDependencyBucket(action)

		and:
		def factory = {
			def configuration = Mock(Configuration) {
				getName() >> identifier.configurationName
			}
			mapperAction.execute(configuration)
			throw new RuntimeException()
		} as DependencyBucketFactory<TestableBucket>
		try {
			subject.guard(identifier, factory)
		} catch (RuntimeException ignored) {}

		when:
		subject.guard(identifier, { bucket } as DependencyBucketFactory<TestableBucket>)

		then:
		0 * action.execute(bucket)
	}

	def "if factory throws e"() {
		given:
		def subject = new DependencyBucketCreationGuard()
		def bucket = Mock(DependencyBucket)
		def identifier = DependencyIdentifier.of(DependencyBucketName.of('implementation'), TestableBucket, ProjectIdentifier.of('root'))

		and:
		def action1 = Mock(Action)
		def action2 = Mock(Action)
		def action3 = Mock(Action)
		def mapperAction1 = subject.mapToDependencyBucket(action1)
		def mapperAction2 = subject.mapToDependencyBucket(action2)
		def mapperAction3 = subject.mapToDependencyBucket(action3)

		and:
		def factory = {
			def configuration = Mock(Configuration) {
				getName() >> identifier.configurationName
			}
			mapperAction1.execute(configuration)
			mapperAction2.execute(configuration)
			mapperAction3.execute(configuration)
			bucket
		} as DependencyBucketFactory<TestableBucket>

		when:
		subject.guard(identifier, factory)

		then:
		1 * action1.execute(bucket)
		and:
		1 * action2.execute(bucket)
		and:
		1 * action3.execute(bucket)
	}

	interface TestableBucket extends DependencyBucket {}
}
