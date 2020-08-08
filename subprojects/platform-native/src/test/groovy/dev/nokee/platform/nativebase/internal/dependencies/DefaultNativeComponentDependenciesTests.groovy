package dev.nokee.platform.nativebase.internal.dependencies

import dev.nokee.platform.base.AbstractComponentDependenciesDelegateTest
import dev.nokee.platform.base.AbstractComponentDependenciesPredefinedBucketsTest
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal
import org.gradle.api.artifacts.Configuration
import spock.lang.Subject

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesDelegateTest extends AbstractComponentDependenciesDelegateTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new DefaultNativeComponentDependencies(delegate)
	}
}

@Subject(DefaultNativeComponentDependencies)
class DefaultNativeComponentDependenciesTest extends AbstractComponentDependenciesPredefinedBucketsTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new DefaultNativeComponentDependencies(delegate)
	}

	@Override
	protected List<String> getBucketNamesUnderTest() {
		return ['implementation', 'compileOnly', 'runtimeOnly', 'linkOnly']
	}

	def "configures predefined buckets"() {
		given:
		def configurations = [:].withDefault { Mock(Configuration) }
		def delegate = Mock(ComponentDependenciesInternal) {
			create(_, _) >> { args ->
				def configuration = configurations[args[0]]
				args[1].execute(configuration)
				return Mock(dev.nokee.platform.base.DependencyBucket) {
					getAsConfiguration() >> configuration
				}
			}
			getComponentDisplayName() >> 'Testing'
		}

		when:
		newSubject(delegate)

		then:
		1 * configurations.implementation.setCanBeConsumed(false)
		1 * configurations.implementation.setCanBeResolved(false)
		1 * configurations.implementation.setDescription("Implementation only dependencies for Testing.")
		0 * configurations.implementation._

		and:
		1 * configurations.compileOnly.setCanBeConsumed(false)
		1 * configurations.compileOnly.setCanBeResolved(false)
		1 * configurations.compileOnly.setDescription("Compile only dependencies for Testing.")
		1 * configurations.compileOnly.extendsFrom(configurations.implementation)
		0 * configurations.compileOnly._

		and:
		1 * configurations.linkOnly.setCanBeConsumed(false)
		1 * configurations.linkOnly.setCanBeResolved(false)
		1 * configurations.linkOnly.setDescription("Link only dependencies for Testing.")
		1 * configurations.linkOnly.extendsFrom(configurations.implementation)
		0 * configurations.linkOnly._

		and:
		1 * configurations.runtimeOnly.setCanBeConsumed(false)
		1 * configurations.runtimeOnly.setCanBeResolved(false)
		1 * configurations.runtimeOnly.setDescription("Runtime only dependencies for Testing.")
		1 * configurations.runtimeOnly.extendsFrom(configurations.implementation)
		0 * configurations.runtimeOnly._
	}
}
