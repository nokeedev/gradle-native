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
package dev.nokee.platform.cpp

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.fixtures.AbstractNativeComponentBuildTypeFunctionalTest
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.fixtures.CppCompileGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

class CppApplicationBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
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
				id 'dev.nokee.cpp-application'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CppGreeterApp().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CppGreeterApp().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CppCompileGreeter()
			}
		}
	}
}

class CppLibraryBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
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
				id 'dev.nokee.cpp-library'
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new CppGreeterLib().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new CppGreeterLib().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new CppCompileGreeter()
			}
		}
	}
}
