package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageIncrementalCompilationFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.swift.SwiftTaskNames

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'root'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'executable'
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryNativeLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithStaticLinkageNativeLanguageIncrementalCompilationFunctionalTest extends SwiftLibraryNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << '''
			library {
				targetLinkages = [linkages.static]
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'staticLibrary'
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithSharedLinkageNativeLanguageIncrementalCompilationFunctionalTest extends SwiftLibraryNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << '''
			library {
				targetLinkages = [linkages.shared]
			}
		'''
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithBothLinkageNativeLanguageIncrementalCompilationFunctionalTest extends SwiftLibraryNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << '''
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		'''
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
	}
}
