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
package dev.nokee.platform.swift

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterLib

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterApp()
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << '''
			rootProject.name = 'application'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
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
				id 'dev.nokee.swift-application'
			}

			application {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames {
	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterLib()
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << '''
			rootProject.name = 'rootLibrary'
		'''
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
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
				id 'dev.nokee.swift-library'
			}

			library {
				dependencies {
					implementation project(':library')
				}
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}
}
