package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'app'"
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
	protected String getBinaryLifecycleTaskName() {
		return 'executable'
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'lib'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeter()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftStaticLibraryNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'lib'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}

			library {
				targetLinkages = [linkages.static]
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeter()
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

// TODO: explicit shared linkage
// TODO: explicit static linkage
// TODO: explicit both linkage
