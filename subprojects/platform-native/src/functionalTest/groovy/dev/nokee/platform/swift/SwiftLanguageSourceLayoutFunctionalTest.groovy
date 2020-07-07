package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeLanguageSourceLayoutFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames {
	def componentUnderTest = new SwiftGreeterApp()

	@Override
	protected void makeSingleComponent() {
		settingsFile << "rootProject.name = 'app'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-application'
			}
		'''
		componentUnderTest.writeToSourceDir(file('srcs'))
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			application {
				sources.from('srcs')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			application {
				${componentUnderTest.files.collect { "sources.from('srcs/${it.name}')" }.join('\n')}
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryNativeLanguageSourceLayoutFunctionalTest extends AbstractNativeLanguageSourceLayoutFunctionalTest implements SwiftTaskNames {
	def componentUnderTest = new SwiftGreeter()

	@Override
	protected void makeSingleComponent() {
		settingsFile << "rootProject.name = 'lib'"
		buildFile << '''
			plugins {
				id 'dev.nokee.swift-library'
			}
		'''
		componentUnderTest.writeToSourceDir(file('srcs'))
	}

	@Override
	protected String configureSourcesAsConvention() {
		return """
			library {
				sources.from('srcs')
			}
		"""
	}

	@Override
	protected String configureSourcesAsExplicitFiles() {
		return """
			library {
				${componentUnderTest.files.collect { "sources.from('srcs/${it.name}')" }.join('\n')}
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithStaticLinkageNativeLanguageSourceLayoutFunctionalTest extends SwiftLibraryNativeLanguageSourceLayoutFunctionalTest {
	@Override
	protected void makeSingleComponent() {
		super.makeSingleComponent()
		buildFile << '''
			library {
				targetLinkages = [linkages.static]
			}
		'''
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithSharedLinkageNativeLanguageSourceLayoutFunctionalTest extends SwiftLibraryNativeLanguageSourceLayoutFunctionalTest {
	@Override
	protected void makeSingleComponent() {
		super.makeSingleComponent()
		buildFile << '''
			library {
				targetLinkages = [linkages.shared]
			}
		'''
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryWithBothLinkageNativeLanguageSourceLayoutFunctionalTest extends SwiftLibraryNativeLanguageSourceLayoutFunctionalTest {
	@Override
	protected void makeSingleComponent() {
		super.makeSingleComponent()
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
