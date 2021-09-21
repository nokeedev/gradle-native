/*
 * Copyright 2020 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
		0 * configurations.api._

		and:
		1 * configurations.jvmImplementation.extendsFrom(configurations.api)
		0 * configurations.jvmImplementation._

		and:
		1 * configurations.jvmRuntimeOnly.extendsFrom(configurations.jvmImplementation)
		0 * configurations.jvmRuntimeOnly._

		and:
		0 * configurations.nativeImplementation._

		and:
		1 * configurations.nativeLinkOnly.extendsFrom(configurations.nativeImplementation)
		0 * configurations.nativeLinkOnly._

		and:
		1 * configurations.nativeRuntimeOnly.extendsFrom(configurations.nativeImplementation)
		0 * configurations.nativeRuntimeOnly._
	}
}
