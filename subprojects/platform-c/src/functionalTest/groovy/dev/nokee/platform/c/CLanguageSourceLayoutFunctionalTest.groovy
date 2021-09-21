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
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib

class CApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterApp()
	}

	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
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
				id 'dev.nokee.c-application'
			}

			application {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}
}

class CLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements CTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterLib()
	}

	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
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
				id 'dev.nokee.c-library'
			}

			library {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}
}
