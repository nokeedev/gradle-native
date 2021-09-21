/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.jni

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.platform.jni.fixtures.KotlinJniCppGreeterLib
import dev.nokee.platform.jni.fixtures.elements.JniLibraryElement

class KotlinCppJniLibraryWithJUnitTestingFunctionalTest extends AbstractInstalledToolChainIntegrationSpec {
	def setup() {
		settingsFile << """
			pluginManagement {
				repositories {
					gradlePluginPortal()
					mavenCentral()
				}

				resolutionStrategy {
					eachPlugin {
						if (requested.id.id == 'org.jetbrains.kotlin.jvm') {
							useModule("org.jetbrains.kotlin:kotlin-gradle-plugin:${kotlinVersion}")
						}
					}
				}
			}
		"""
	}

	def "can test library using JUnit"() {
		makeSingleProject()
		buildFile << '''
			repositories {
				mavenCentral()
			}

			dependencies {
				testImplementation 'org.jetbrains.kotlin:kotlin-test'
				testImplementation 'org.jetbrains.kotlin:kotlin-test-junit'
			}
		'''
		componentUnderTest.writeToProject(testDirectory)

		expect:
		succeeds('test')
		// TODO: assert JUnit result
	}

	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'org.jetbrains.kotlin.jvm'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
			}

			repositories {
				mavenCentral()
			}

			dependencies {
				implementation platform('org.jetbrains.kotlin:kotlin-bom:${kotlinVersion}')
				implementation 'org.jetbrains.kotlin:kotlin-stdlib-jdk8'
			}
		"""
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	JniLibraryElement getComponentUnderTest() {
		return new KotlinJniCppGreeterLib('jni-greeter').withJUnitTest()
	}

	private static String getKotlinVersion() {
		return '1.5.10'
	}
}
