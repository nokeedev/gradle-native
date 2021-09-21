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
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib

class CTestSuiteLibraryFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-library'
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
		return new CGreeterLib().withGenericTestSuite()
	}

	@Override
	protected NativeProjectTasks getTasksUnderTest() {
		return tasks.withComponentName('test').forSharedLibrary
	}
}

class CTestSuiteApplicationFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
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
		return new CGreeterApp().withGenericTestSuite()
	}
}
