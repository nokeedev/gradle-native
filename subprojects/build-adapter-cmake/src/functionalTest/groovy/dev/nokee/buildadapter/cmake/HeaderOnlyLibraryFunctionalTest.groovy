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
