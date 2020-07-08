package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.swift-application'
			}
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.swift-library'
			}
		"""
		new SwiftGreeterApp().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'application'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").allToLink
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentProjectDependenciesWithStaticLinkageFunctionalTest extends SwiftApplicationComponentProjectDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").forStaticLibrary.allToLinkOrCreate
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentProjectDependenciesWithSharedLinkageFunctionalTest extends SwiftApplicationComponentProjectDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentProjectDependenciesWithBothLinkageFunctionalTest extends SwiftApplicationComponentProjectDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").withLinkage('shared').allToLink
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.swift-application'
			}
		"""
		file(libraryProjectName, settingsFileName) << """
			rootProject.name = '${libraryProjectName}'
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.swift-library'
			}
		""" << configureLibraryProject()
		new SwiftGreeterApp().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'application'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").allToLink
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentIncludedBuildDependenciesWithStaticLinkageFunctionalTest extends SwiftApplicationComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").forStaticLibrary.allToLinkOrCreate
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentIncludedBuildDependenciesWithSharedLinkageFunctionalTest extends SwiftApplicationComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftApplicationComponentIncludedBuildDependenciesWithBothLinkageFunctionalTest extends SwiftApplicationComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		file(libraryProjectName, buildFileName) << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").withLinkage('shared').allToLink
	}
}

// TODO: Add library to library dependencies
// TODO: Add shared library to static library dependencies
// TODO: Add app to static library dependencies
