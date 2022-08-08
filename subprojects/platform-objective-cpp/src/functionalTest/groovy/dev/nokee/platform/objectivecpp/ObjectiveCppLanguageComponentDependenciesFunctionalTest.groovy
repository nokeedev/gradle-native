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
package dev.nokee.platform.objectivecpp

import dev.gradleplugins.integtests.fixtures.nativeplatform.RequiresInstalledToolChain
import dev.gradleplugins.integtests.fixtures.nativeplatform.ToolChainRequirement
import dev.nokee.fixtures.AbstractNativeComponentIncludedBuildDependenciesFunctionalTest
import dev.nokee.fixtures.AbstractNativeComponentProjectDependenciesFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.objectivecpp.ObjectiveCppTaskNames
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterApp
import dev.nokee.platform.nativebase.fixtures.ObjectiveCppGreeterLib
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
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentProjectDependenciesFunctionalTest extends AbstractNativeComponentProjectDependenciesFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
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
		new ObjectiveCppGreeterLib().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentWithStaticLinkageProjectDependenciesFunctionalTest extends ObjectiveCppLibraryComponentProjectDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentWithSharedLinkageProjectDependenciesFunctionalTest extends ObjectiveCppLibraryComponentProjectDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentWithBothLinkageProjectDependenciesFunctionalTest extends ObjectiveCppLibraryComponentProjectDependenciesFunctionalTest {
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
		return tasks(":${libraryProjectName}").allToLinkElements
	}
}

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentIncludedBuildDependenciesFunctionalTest extends AbstractNativeComponentIncludedBuildDependenciesFunctionalTest implements ObjectiveCppTaskNames {
	@Override
	protected void makeComponentWithLibrary() {
		settingsFile << configureMultiProjectBuild()
		buildFile << """
			plugins {
				id 'dev.nokee.objective-cpp-library'
			}

			tasks.withType(LinkSharedLibrary).configureEach {
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
		new ObjectiveCppGreeterLib().withImplementationAsSubproject(libraryProjectName).writeToProject(testDirectory)
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentWithStaticLinkageIncludedBuildDependenciesFunctionalTest extends ObjectiveCppLibraryComponentIncludedBuildDependenciesFunctionalTest {
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

@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentWithSharedLinkageIncludedBuildDependenciesFunctionalTest extends ObjectiveCppLibraryComponentIncludedBuildDependenciesFunctionalTest {
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


@RequiresInstalledToolChain(ToolChainRequirement.GCC_COMPATIBLE)
@Requires({!OperatingSystem.current.windows})
class ObjectiveCppLibraryComponentWithBothLinkageIncludedBuildDependenciesFunctionalTest extends ObjectiveCppLibraryComponentIncludedBuildDependenciesFunctionalTest {
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
