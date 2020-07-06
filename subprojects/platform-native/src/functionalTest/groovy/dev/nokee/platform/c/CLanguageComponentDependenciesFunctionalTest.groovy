package dev.nokee.platform.c


import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.platform.nativebase.fixtures.CMainUsesGreeter

class CApplicationComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements CTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
			}
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.c-library'
			}
		"""
		new CMainUsesGreeter().writeToProject(testDirectory)
		new CGreeter().asLib().writeToProject(testDirectory.file(libraryProjectName))
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

class CApplicationComponentProjectDependenciesWithStaticLinkageFunctionalTest extends CApplicationComponentProjectDependenciesFunctionalTest {
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

class CApplicationComponentProjectDependenciesWithSharedLinkageFunctionalTest extends CApplicationComponentProjectDependenciesFunctionalTest {
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

class CApplicationComponentProjectDependenciesWithBothLinkageFunctionalTest extends CApplicationComponentProjectDependenciesFunctionalTest {
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

class CApplicationComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements CTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
			}
		"""
		file(libraryProjectName, settingsFileName) << """
			rootProject.name = '${libraryProjectName}'
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.c-library'
			}
		""" << configureLibraryProject()
		new CMainUsesGreeter().writeToProject(testDirectory)
		new CGreeter().asLib().writeToProject(testDirectory.file(libraryProjectName))
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

class CApplicationComponentIncludedBuildDependenciesToStaticLinkageFunctionalTest extends CApplicationComponentIncludedBuildDependenciesFunctionalTest {
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

class CApplicationComponentIncludedBuildDependenciesToSharedLinkageFunctionalTest extends CApplicationComponentIncludedBuildDependenciesFunctionalTest {
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

class CApplicationComponentIncludedBuildDependenciesToBothLinkageFunctionalTest extends CApplicationComponentIncludedBuildDependenciesFunctionalTest {
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
