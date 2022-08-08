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
package dev.nokee.platform.swift

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.swift.SwiftTaskNames
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterApp
import dev.nokee.platform.nativebase.fixtures.SwiftGreeterLib

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
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << "rootProject.name = 'root'"
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.swift-library'
			}
		"""
		file(libraryProjectName, buildFileName) << """
			plugins {
				id 'dev.nokee.swift-library'
			}
		"""
		new SwiftGreeterLib().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
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

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentWithStaticLinkageProjectDependenciesFunctionalTest extends SwiftLibraryComponentProjectDependenciesFunctionalTest {
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
		return [tasks(":${libraryProjectName}").compile]
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentWithSharedLinkageProjectDependenciesFunctionalTest extends SwiftLibraryComponentProjectDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentWithBothLinkageProjectDependenciesFunctionalTest extends SwiftLibraryComponentProjectDependenciesFunctionalTest {
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
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements SwiftTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << "rootProject.name = 'root'"
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.swift-library'
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
		new SwiftGreeterLib().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
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

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentWithStaticLinkageIncludedBuildDependenciesFunctionalTest extends SwiftLibraryComponentIncludedBuildDependenciesFunctionalTest {
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
		return [tasks(":${libraryProjectName}").compile]
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentWithSharedLinkageIncludedBuildDependenciesFunctionalTest extends SwiftLibraryComponentIncludedBuildDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.SWIFTC)
class SwiftLibraryComponentWithBothLinkageIncludedBuildDependenciesFunctionalTest extends SwiftLibraryComponentIncludedBuildDependenciesFunctionalTest {
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
