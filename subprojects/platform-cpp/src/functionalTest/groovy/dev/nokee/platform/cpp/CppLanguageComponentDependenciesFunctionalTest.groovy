/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.platform.cpp

import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

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
		new CppGreeterApp().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'application'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

class CppLibraryComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements CppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
			}
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.cpp-library'
			}
		"""
		new CppGreeterLib().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

class CppLibraryComponentWithStaticLinkageProjectDependenciesFunctionalTest extends CppLibraryComponentProjectDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getLibraryTasks() {
		return [tasks(':library').syncApiElements]
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

class CppLibraryComponentWithSharedLinkageProjectDependenciesFunctionalTest extends CppLibraryComponentProjectDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		buildFile << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

class CppLibraryComponentWithBothLinkageProjectDependenciesFunctionalTest extends CppLibraryComponentProjectDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
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
		new CppGreeterApp().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'application'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

class CppLibraryComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements CppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
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
		new CppGreeterLib().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
	}

	@Override
	protected String getComponentUnderTestDsl() {
		return 'library'
	}

	@Override
	protected List<String> getLibraryTasks() {
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

class CppLibraryComponentWithStaticLinkageIncludedBuildDependenciesFunctionalTest extends CppLibraryComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		buildFile << """
			library {
				targetLinkages = [linkages.static]
			}
		"""
	}

	@Override
	protected List<String> getLibraryTasks() {
		return [tasks(':library').syncApiElements]
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

class CppLibraryComponentWithSharedLinkageIncludedBuildDependenciesFunctionalTest extends CppLibraryComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		buildFile << """
			library {
				targetLinkages = [linkages.shared]
			}
		"""
	}
}

class CppLibraryComponentWithBothLinkageIncludedBuildDependenciesFunctionalTest extends CppLibraryComponentIncludedBuildDependenciesFunctionalTest {
	@Override
	protected void makeComponentWithLibrary() {
		super.makeComponentWithLibrary()
		buildFile << """
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		"""
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
	}
}
