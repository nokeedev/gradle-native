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
		return "app"
	}

	@Override
	protected String getSchemeName() {
		return "App"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}
}

class XcodeIdeSwiftApplicationWithoutSourceFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
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
		return SourceElement.empty()
	}

	@Override
	protected String getProjectName() {
		return "app"
	}

	@Override
	protected String getSchemeName() {
		return "App"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
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
		return "lib"
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

class XcodeIdeSwiftLibraryWithoutSourceFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest implements SwiftTaskNames {
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
		return SourceElement.empty()
	}

	@Override
	protected String getProjectName() {
		return "lib"
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
