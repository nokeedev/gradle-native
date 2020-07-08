package dev.nokee.platform.objectivecpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp
import spock.lang.Requires
import spock.util.environment.OperatingSystem

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.objective-cpp-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		"""
		new ObjectiveCppGreeterApp().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentProjectDependenciesWithStaticLinkageFunctionalTest extends ObjectiveCppApplicationComponentProjectDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentProjectDependenciesWithSharedLinkageFunctionalTest extends ObjectiveCppApplicationComponentProjectDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentProjectDependenciesWithBothLinkageFunctionalTest extends ObjectiveCppApplicationComponentProjectDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-application'
			}

			tasks.withType(LinkExecutable).configureEach {
				linkerArgs.add('-lobjc')
			}
		"""
		file(libraryProjectName, settingsFileName) << """
			rootProject.name = '${libraryProjectName}'
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.objective-cpp-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
				linkerArgs.add('-lobjc')
			}
		""" << configureLibraryProject()
		new ObjectiveCppGreeterApp().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentIncludedBuildDependenciesWithStaticLinkageFunctionalTest extends ObjectiveCppApplicationComponentIncludedBuildDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentIncludedBuildDependenciesWithSharedLinkageFunctionalTest extends ObjectiveCppApplicationComponentIncludedBuildDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppApplicationComponentIncludedBuildDependenciesWithBothLinkageFunctionalTest extends ObjectiveCppApplicationComponentIncludedBuildDependenciesFunctionalTest {
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
