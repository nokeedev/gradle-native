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
package dev.nokee.platform.objectivec

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.fixtures.AbstractNativeComponentBuildTypeFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.nativebase.fixtures.ObjectiveCCompileGreeter
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterLib
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCApplicationBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements ObjectiveCTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
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
				id 'dev.nokee.objective-c-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new ObjectiveCGreeterApp().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new ObjectiveCGreeterApp().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new ObjectiveCCompileGreeter()
			}
		}
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryBuildTypeFunctionalTest extends AbstractNativeComponentBuildTypeFunctionalTest implements ObjectiveCTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
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
				id 'dev.nokee.objective-c-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
		file('library', buildFileName) << '''
			plugins {
				id 'dev.nokee.objective-c-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		'''
	}

	@Override
	protected ComponentUnderTest getComponentUnderTest() {
		return new AbstractNativeComponentBuildTypeFunctionalTest.ComponentUnderTest() {
			@Override
			void writeToProject(TestFile projectDirectory) {
				new ObjectiveCGreeterLib().writeToProject(projectDirectory)
			}

			@Override
			SourceElement withImplementationAsSubproject(String subprojectPath) {
				return new ObjectiveCGreeterLib().withImplementationAsSubproject(subprojectPath)
			}

			@Override
			SourceElement withPreprocessorImplementation() {
				return new ObjectiveCCompileGreeter()
			}
		}
	}
}
