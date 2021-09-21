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
import dev.nokee.fixtures.AbstractTargetMachinesFunctionalTest
import dev.nokee.language.objectivec.ObjectiveCTaskNames
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCGreeterLib
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCApplicationTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements ObjectiveCTaskNames {
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
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeterApp()
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			executable("build/exes/main/${projectName}").exec().out == 'Bonjour, Alice!'
		} else {
			executable("build/exes/main/${variant}/${projectName}").exec().out == 'Bonjour, Alice!'
		}
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCLibraryTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements ObjectiveCTaskNames {
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
	protected SourceElement getComponentUnderTest() {
		return new ObjectiveCGreeterLib()
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			sharedLibrary("build/libs/main/${projectName}").assertExists()
		} else {
			sharedLibrary("build/libs/main/${variant}/${projectName}").assertExists()
		}
	}
}
