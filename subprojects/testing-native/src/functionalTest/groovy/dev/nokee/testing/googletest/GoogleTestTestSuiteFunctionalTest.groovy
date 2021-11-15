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
package dev.nokee.testing.googletest

import dev.gradleplugins.integtests.fixtures.nativeplatform.AbstractInstalledToolChainIntegrationSpec
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib
import dev.nokee.platform.nativebase.fixtures.GoogleTestGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite
import org.apache.commons.lang3.SystemUtils
import spock.lang.IgnoreIf

class GoogleTestTestSuiteFunctionalTest extends AbstractInstalledToolChainIntegrationSpec implements CppTaskNames {
	@IgnoreIf({ SystemUtils.IS_OS_WINDOWS }) // because left over daemons cause failures
	def "can test using Google Test built from source"() {
		given:
		settingsFile << '''
			sourceControl {
				gitRepository("https://github.com/google/googletest") {
					producesModule("com.github.google.googletest:gtest")
					producesModule("com.github.google.googletest:gtest_main")
					plugins {
						id 'dev.nokee.cmake-build-adapter'
					}
				}
			}
		'''
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}
			import ${ExecutableBinary.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent library
					dependencies {
						implementation('com.github.google.googletest:gtest:latest.integration') {
							version {
								branch = 'main'
							}
						}
						implementation('com.github.google.googletest:gtest_main:latest.integration') {
							version {
								branch = 'main'
							}
						}
					}
					binaries.configureEach(${ExecutableBinary.simpleName}) {
						compileTasks.configureEach {
							compilerArgs.addAll(toolChain.map {
								if (it in Gcc || it in Clang) {
									return ['-std=c++17']
								}
								return []
							})
						}
						linkTask.configure {
							linkerArgs.addAll(targetPlatform.map {
								if (it.operatingSystem.linux) {
									return ['-lpthread']
								} else if (it.operatingSystem.windows) {
									return ['/SUBSYSTEM:CONSOLE']
								}
								return []
							})
						}
					}
				}
			}
		"""

		new GoogleTestGreeterTest().writeToProject(testDirectory)
		new CppGreeterLib().writeToProject(testDirectory)

		when:
		// Because of https://github.com/gradle/gradle/issues/13793
		executer = executer.withoutDeprecationChecks()
		succeeds('test')

		then:
		result.assertTasksExecuted(tasks.compile, tasks.withComponentName('test').forSharedLibrary.allToTest, ':googletest:gtest:make', ':googletest:gtest_main:make')
	}

	// TODO: Test failing test suite
}
