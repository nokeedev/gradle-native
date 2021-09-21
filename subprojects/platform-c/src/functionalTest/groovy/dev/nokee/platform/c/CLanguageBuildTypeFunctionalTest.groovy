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
package dev.nokee.platform.c

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.fixtures.AbstractNativeComponentBuildTypeFunctionalTest
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.fixtures.CCompileGreeter
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib

class CApplicationBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
			}
		'''
	}

	@Override
	protected void makeMultiProjectWithoutDependency() {
		settingsFile << '''
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CGreeterApp().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CGreeterApp().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CCompileGreeter()
			}
		}
	}
}

class CLibraryBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected void makeMultiProjectWithoutDependency() {
		settingsFile << '''
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CGreeterLib().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CGreeterLib().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CCompileGreeter()
			}
		}
	}
}
