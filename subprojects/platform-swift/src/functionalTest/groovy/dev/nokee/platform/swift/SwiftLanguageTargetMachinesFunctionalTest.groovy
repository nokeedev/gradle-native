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
