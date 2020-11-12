package dev.nokee.platform.ios

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageIncrementalCompilationFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.ios.fixtures.IosTaskNames
import spock.lang.Requires

@Requires({ os.macOs })
@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftIosApplicationNativeLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest implements SwiftTaskNames, IosTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-ios-application'
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'bundle'
	}
}
