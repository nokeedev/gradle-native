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

class BinaryLinkageSelectionFunctionalTest extends AbstractGradleSpecification {

	def setup() {
		settingsFile << '''
			include 'lib'
		'''
		file('lib/build.gradle') << """
			import ${BinaryLinkage.canonicalName}
			configurations {
				sharedElements {
					canBeConsumed = true
					canBeResolved = false
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.SHARED))
					}
					outgoing.artifact(file('shared'))
				}
				staticElements {
					canBeConsumed = true
					canBeResolved = false
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.STATIC))
					}
					outgoing.artifact(file('static'))
				}
			}
		"""
		buildFile << """
			plugins {
				id 'dev.nokee.native-runtime'
			}

			import ${BinaryLinkage.canonicalName}

			configurations.create('test') {
				canBeConsumed = false
				canBeResolved = true
			}

			def verifyTask = tasks.register('verify')
		"""
	}

	def "can resolve explicit static linkage"() {
		buildFile << '''
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.STATIC))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'static'
				}
			}
		'''

		expect:
		succeeds('verify')
	}

	def "can resolve explicit shared linkage"() {
		buildFile << '''
			dependencies {
				test(project(':lib')) {
					attributes {
						attribute(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE, objects.named(BinaryLinkage, BinaryLinkage.SHARED))
					}
				}
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'shared'
				}
			}
		'''

		expect:
		succeeds('verify')
	}

	def "defaults to shared linkage when none is provided"() {
		buildFile << '''
			dependencies {
				test(project(':lib'))
			}

			verifyTask.configure {
				doLast {
					assert configurations.test.singleFile.name == 'shared'
				}
			}
		'''

		expect:
		succeeds('verify')
	}
}
