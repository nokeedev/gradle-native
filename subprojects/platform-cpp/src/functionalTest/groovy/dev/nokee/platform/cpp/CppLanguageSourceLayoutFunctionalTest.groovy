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
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

class CppApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CppTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp()
	}

	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
			}
		'''
	}

	@Override
	protected void makeProjectWithLibrary() {
		settingsFile << '''
			rootProject.name = 'application'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
			}

			application {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}
}

class CppLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CppTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterLib()
	}

	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected void makeProjectWithLibrary() {
		settingsFile << '''
			rootProject.name = 'rootLibrary'
			include 'library'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}

			library {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}
}
