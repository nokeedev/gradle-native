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
import dev.nokee.fixtures.AbstractTargetMachinesFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterLib

import static org.junit.Assume.assumeTrue

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterApp()
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			executable("build/exes/main/${projectName.capitalize()}").exec().out == 'Bonjour, Alice!'
		} else {
			executable("build/exes/main/${variant}/${projectName.capitalize()}").exec().out == 'Bonjour, Alice!'
		}
	}

	@Override
	protected String configureToolChainSupport(String operatingSystem, String architecture) {
		assumeTrue(false); // Swift is a bit troublesome to align with the other language behaviour until we clean up the mess inside Gradle.
		return super.configureToolChainSupport(operatingSystem, architecture)
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterLib()
	}

	@Override
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			sharedLibrary("build/libs/main/${projectName.capitalize()}").assertExists()
		} else {
			sharedLibrary("build/libs/main/${variant}/${projectName.capitalize()}").assertExists()
		}
	}

	@Override
	protected String configureToolChainSupport(String operatingSystem, String architecture) {
		assumeTrue(false); // Swift is a bit troublesome to align with the other language behaviour until we clean up the mess inside Gradle.
		return super.configureToolChainSupport(operatingSystem, architecture)
	}
}
