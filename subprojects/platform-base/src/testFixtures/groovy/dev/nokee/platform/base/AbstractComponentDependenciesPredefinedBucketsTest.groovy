package dev.nokee.platform.base

import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal
import org.gradle.api.Action
import org.gradle.api.artifacts.Configuration
import spock.lang.Specification
import spock.lang.Unroll

abstract class AbstractComponentDependenciesPredefinedBucketsTest extends Specification {
	final ComponentDependenciesInternal delegate = Mock()

	protected abstract BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate)

	protected abstract List<String> getBucketNamesUnderTest()

	def "creates predefined dependency buckets"() {
		when:
		def subject = newSubject(delegate)

		then:
		interaction {
			for (def name : bucketNamesUnderTest) {
				1 * this.delegate.create(name, _)
			}
			0 * _
		}
	}

	@Unroll
	def "uses the correct bucket"(bucketName) {
		given:
		def buckets = [:].withDefault { Mock(DependencyBucket) }
		delegate.create(_, _) >> { String name, Action action -> buckets[name] }
		def subject = newSubject(delegate)
		def notation = new Object()
		def action = Mock(Action)

		when:
		subject."${bucketName}"(notation)
		then:
		1 * buckets[bucketName].addDependency(notation)
		0 * _

		when:
		subject."${bucketName}"(notation, action)
		then:
		1 * buckets[bucketName].addDependency(notation, action)
		0 * _

		when:
		def bucket = subject."get${bucketName.capitalize()}"()
		then:
		bucket == buckets[bucketName]

		where:
		bucketName << bucketNamesUnderTest
	}
}
