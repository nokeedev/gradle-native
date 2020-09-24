package dev.nokee.platform.nativebase.internal.dependencies

import dev.nokee.platform.base.AbstractComponentDependenciesDelegateTest
import dev.nokee.platform.base.AbstractComponentDependenciesPredefinedBucketsTest
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal
import org.gradle.api.artifacts.Configuration
import spock.lang.Subject

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesDelegateTest extends AbstractComponentDependenciesDelegateTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new DefaultNativeLibraryComponentDependencies(delegate)
	}
}

@Subject(DefaultNativeLibraryComponentDependencies)
class DefaultNativeLibraryComponentDependenciesTest extends AbstractComponentDependenciesPredefinedBucketsTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new DefaultNativeLibraryComponentDependencies(delegate)
	}

	@Override
	protected List<String> getBucketNamesUnderTest() {
		return ['api', 'implementation', 'compileOnly', 'runtimeOnly', 'linkOnly']
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
		0 * configurations.api._

		and:
		1 * configurations.implementation.extendsFrom(configurations.api)
		0 * configurations.implementation._

		and:
		1 * configurations.compileOnly.extendsFrom(configurations.implementation)
		0 * configurations.compileOnly._

		and:
		1 * configurations.linkOnly.extendsFrom(configurations.implementation)
		0 * configurations.linkOnly._

		and:
		1 * configurations.runtimeOnly.extendsFrom(configurations.implementation)
		0 * configurations.runtimeOnly._
	}
}
