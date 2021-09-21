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
package dev.nokee.testing.nativebase

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

class CppTestSuiteLibraryFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
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
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterLib().withGenericTestSuite()
	}

	@Override
	protected NativeProjectTasks getTasksUnderTest() {
		return tasks.withComponentName('test').forSharedLibrary
	}
}

class CppTestSuiteApplicationFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}
			import ${ExecutableBinary.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent application
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp().withGenericTestSuite()
	}
}
