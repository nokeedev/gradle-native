package dev.nokee.platform.ios

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import dev.nokee.platform.ios.fixtures.SwiftIosApp

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftIosApplicationNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements SwiftTaskNames, IosTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'app'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-ios-application'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftIosApp()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'bundle'
	}

	@Override
	protected boolean isTargetMachineAwareConfiguration() {
		return false
	}
}
