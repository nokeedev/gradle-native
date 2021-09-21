/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.runtime.nativebase

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import spock.lang.Unroll

class BuildTypeSelectionFunctionalTest extends AbstractGradleSpecification {
	def setup() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			import ${BuildType.canonicalName}
		"""
		buildFile << """
			plugins {
				id 'dev.nokee.native-runtime'
			}

			import ${BuildType.canonicalName}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
			}

			def verifyTask = tasks.register('verify')
		"""
	}

	@Unroll
	def "resolves to exact build type match"(String buildType) {
		file('lib/build.gradle') <<
			// Different capitalization build types
			createResolvableConfiguration('Release') <<
			createResolvableConfiguration('Debug') <<
			createResolvableConfiguration('relwithdebug') <<
			// Actual build type to resolve
			createResolvableConfiguration(buildType)
		buildFile << """
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BuildType.BUILD_TYPE_ATTRIBUTE, objects.named(BuildType, '${buildType}'))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == '${buildType}'
				}
			}
		"""

		expect:
		succeeds('verify')

		where:
		buildType << ['release', 'debug', 'RelWithDebug']
	}

	def "resolves the only available build type"() {
		file('lib/build.gradle') << createResolvableConfiguration('my-build-type')
		buildFile << """
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BuildType.BUILD_TYPE_ATTRIBUTE, objects.named(BuildType, 'debug'))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'my-build-type'
				}
			}
		"""

		expect:
		succeeds('verify')
	}

	@Unroll
	def "resolves to similar looking build type"(String buildType, String expected) {
		file('lib/build.gradle') <<
			createResolvableConfiguration('Release') <<
			createResolvableConfiguration('Debug') <<
			createResolvableConfiguration('relwithdebug')
		buildFile << """
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BuildType.BUILD_TYPE_ATTRIBUTE, objects.named(BuildType, '${buildType}'))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == '${expected}'
				}
			}
		"""

		expect:
		succeeds('verify')

		where:
		buildType 		| expected
		'release' 		| 'Release'
		'debug' 		| 'Debug'
		'RelWithDebug'	| 'relwithdebug'
	}

	@Unroll
	def "resolve to debug-like build type when none provided"(String debugBuildType) {
		file('lib/build.gradle') << createResolvableConfiguration(debugBuildType) <<
			createResolvableConfiguration('release') <<
			createResolvableConfiguration('my-build-type')
		buildFile << """
			dependencies {
				test(project(':lib'))
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == '${debugBuildType}'
				}
			}
		"""

		expect:
		succeeds('verify')

		where:
		debugBuildType << ['debug', 'Debug', 'DEbug', 'RelWithDebug']
	}

	private static String createResolvableConfiguration(String name) {
		return """
			configurations.create('${name}Elements') {
				canBeConsumed = true
				canBeResolved = false
				attributes {
					attribute(BuildType.BUILD_TYPE_ATTRIBUTE, objects.named(BuildType, '${name}'))
				}
				outgoing.artifact(file('${name}'))
			}
		"""
	}
}
