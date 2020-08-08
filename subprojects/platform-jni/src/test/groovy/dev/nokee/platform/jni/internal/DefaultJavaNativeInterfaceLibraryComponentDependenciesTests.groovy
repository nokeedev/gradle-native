package dev.nokee.platform.jni.internal

import dev.nokee.platform.base.AbstractComponentDependenciesDelegateTest
import dev.nokee.platform.base.AbstractComponentDependenciesPredefinedBucketsTest
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal
import org.gradle.api.artifacts.Configuration
import spock.lang.Subject

@Subject(DefaultJavaNativeInterfaceLibraryComponentDependencies)
class DefaultJavaNativeInterfaceLibraryComponentDependenciesDelegateTest extends AbstractComponentDependenciesDelegateTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new DefaultJavaNativeInterfaceLibraryComponentDependencies(delegate)
	}
}

@Subject(DefaultJavaNativeInterfaceLibraryComponentDependencies)
class DefaultJavaNativeInterfaceLibraryComponentDependenciesTest extends AbstractComponentDependenciesPredefinedBucketsTest {
	@Override
	protected BaseComponentDependencies newSubject(ComponentDependenciesInternal delegate) {
		return new DefaultJavaNativeInterfaceLibraryComponentDependencies(delegate)
	}

	@Override
	protected List<String> getBucketNamesUnderTest() {
		return ['api', 'jvmImplementation', 'jvmRuntimeOnly', 'nativeImplementation', 'nativeRuntimeOnly', 'nativeLinkOnly']
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
		1 * configurations.api.setCanBeConsumed(false)
		1 * configurations.api.setCanBeResolved(false)
		1 * configurations.api.setDescription("API dependencies for Testing.")
		0 * configurations.api._

		and:
		1 * configurations.jvmImplementation.setCanBeConsumed(false)
		1 * configurations.jvmImplementation.setCanBeResolved(false)
		1 * configurations.jvmImplementation.setDescription("Implementation only dependencies for Testing.")
		1 * configurations.jvmImplementation.extendsFrom(configurations.api)
		0 * configurations.jvmImplementation._

		and:
		1 * configurations.jvmRuntimeOnly.setCanBeConsumed(false)
		1 * configurations.jvmRuntimeOnly.setCanBeResolved(false)
		1 * configurations.jvmRuntimeOnly.setDescription("Runtime only dependencies for Testing.")
		1 * configurations.jvmRuntimeOnly.extendsFrom(configurations.jvmImplementation)
		0 * configurations.jvmRuntimeOnly._

		and:
		1 * configurations.nativeImplementation.setCanBeConsumed(false)
		1 * configurations.nativeImplementation.setCanBeResolved(false)
		1 * configurations.nativeImplementation.setDescription("Implementation only dependencies for Testing.")
		0 * configurations.nativeImplementation._

		and:
		1 * configurations.nativeLinkOnly.setCanBeConsumed(false)
		1 * configurations.nativeLinkOnly.setCanBeResolved(false)
		1 * configurations.nativeLinkOnly.setDescription("Link only dependencies for Testing.")
		1 * configurations.nativeLinkOnly.extendsFrom(configurations.nativeImplementation)
		0 * configurations.nativeLinkOnly._

		and:
		1 * configurations.nativeRuntimeOnly.setCanBeConsumed(false)
		1 * configurations.nativeRuntimeOnly.setCanBeResolved(false)
		1 * configurations.nativeRuntimeOnly.setDescription("Runtime only dependencies for Testing.")
		1 * configurations.nativeRuntimeOnly.extendsFrom(configurations.nativeImplementation)
		0 * configurations.nativeRuntimeOnly._
	}
}
