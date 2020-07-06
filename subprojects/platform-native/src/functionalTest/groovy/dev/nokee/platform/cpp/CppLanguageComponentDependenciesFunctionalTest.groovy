package dev.nokee.platform.cpp

import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppMainUsesGreeter

class CppApplicationComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements CppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
			}
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.cpp-library'
			}
		"""
		new CppMainUsesGreeter().writeToProject(testDirectory)
		new CppGreeter().asLib().writeToProject(testDirectory.file(libraryProjectName))
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

class CppApplicationComponentProjectDependenciesWithStaticLinkageFunctionalTest extends CppApplicationComponentProjectDependenciesFunctionalTest {
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

class CppApplicationComponentProjectDependenciesWithSharedLinkageFunctionalTest extends CppApplicationComponentProjectDependenciesFunctionalTest {
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

class CppApplicationComponentProjectDependenciesWithBothLinkageFunctionalTest extends CppApplicationComponentProjectDependenciesFunctionalTest {
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

class CppApplicationComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements CppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
			}
		"""
		file(libraryProjectName, settingsFileName) << """
			rootProject.name = '${libraryProjectName}'
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.cpp-library'
			}
		""" << configureLibraryProject()
		new CppMainUsesGreeter().writeToProject(testDirectory)
		new CppGreeter().asLib().writeToProject(testDirectory.file(libraryProjectName))
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

class CppApplicationComponentIncludedBuildDependenciesToStaticLinkageFunctionalTest extends CppApplicationComponentIncludedBuildDependenciesFunctionalTest {
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

class CppApplicationComponentIncludedBuildDependenciesToSharedLinkageFunctionalTest extends CppApplicationComponentIncludedBuildDependenciesFunctionalTest {
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

class CppApplicationComponentIncludedBuildDependenciesToBothLinkageFunctionalTest extends CppApplicationComponentIncludedBuildDependenciesFunctionalTest {
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
