package dev.nokee.ide.xcode

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.jni.fixtures.elements.SwiftGreeter
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftApplicationFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-application'
				id 'dev.nokee.xcode-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeterApp()
	}

	@Override
	protected String getProjectName() {
		return "App"
	}

	@Override
	protected String getSchemeName() {
		return "App"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}

	@Override
	protected List<String> getAllTasksToXcode() {
		return super.getAllTasksToXcode() + [tasks.compile]
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.swift-library'
				id 'dev.nokee.xcode-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new SwiftGreeter()
	}

	@Override
	protected String getProjectName() {
		return "Lib"
	}

	@Override
	protected String getSchemeName() {
		return "Lib"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}

	@Override
	protected List<String> getAllTasksToXcode() {
		return super.getAllTasksToXcode() + [tasks.compile]
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithStaticLinkageFunctionalTest extends XcodeIdeSwiftLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.forStaticLibrary.allToCreate
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithSharedLinkageFunctionalTest extends XcodeIdeSwiftLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class XcodeIdeSwiftLibraryWithBothLinkageFunctionalTest extends XcodeIdeSwiftLibraryFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.withLinkage('shared').allToLink
	}

	@Override
	protected String getSchemeName() {
		return "${super.getSchemeName()}Shared"
	}

	@Override
	protected List<String> getAllTasksToXcode() {
		return super.getAllTasksToXcode() - [tasks.compile] + [tasks.withLinkage('shared').compile, tasks.withLinkage('static').compile]
	}
}
