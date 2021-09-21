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
package dev.nokee.buildadapter.cmake

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.language.cpp.CppTaskNames

class HeaderOnlyLibraryFunctionalTest extends AbstractGradleSpecification implements CppTaskNames {
	def "can depend on plog header-only library"() {
		given:
		settingsFile << """
			sourceControl {
				gitRepository("https://github.com/SergiusTheBest/plog") {
					producesModule("com.github.SergiusTheBest.plog:plog-headers")
					plugins {
						id 'dev.nokee.cmake-build-adapter'
					}
				}
			}
		"""
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
			}

			application {
				dependencies {
					implementation 'com.github.SergiusTheBest.plog:plog-headers:1.1.5'
				}
			}
		"""
		file('src/main/cpp/main.cpp') << '''
			#include <plog/Log.h>
			int main() {
				return 0;
			}
		'''

		expect:
		succeeds('assemble')
		result.assertTasksExecutedAndNotSkipped(tasks.allToAssemble)
	}
}
