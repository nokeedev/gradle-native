/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.visualstudio

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib
import dev.nokee.platform.nativebase.fixtures.CGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite
import org.junit.Assume

class VisualStudioIdeCApplicationFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterApp()
	}

	@Override
	protected String configureCustomSourceLayout() {
		return '''
			application {
				cSources.from('srcs')
				privateHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "app"
	}

	@Override
	protected List<String> allTasksForBuildAction(String variant) {
		return tasks.withOperatingSystemFamily(variant).allToLink
	}
}

class VisualStudioIdeCApplicationWithNativeTestSuiteFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
				id 'dev.nokee.visual-studio-ide'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent application
				}
			}
		"""
		new CGreeterApp().writeToProject(testDirectory)
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterTest()
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return '''
			application {
				sources.from('srcs')
				privateHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "app-test"
	}

	@Override
	protected List<String> allTasksForBuildAction(String variant) {
		return [tasks.withOperatingSystemFamily(variant).compile] + tasks.withComponentName('test').withOperatingSystemFamily(variant).allToLink + [tasks.withComponentName('test').withOperatingSystemFamily(variant).relocateMainSymbol]
	}
}

class VisualStudioIdeCLibraryFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-library'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterLib()
	}

	@Override
	protected String configureCustomSourceLayout() {
		return '''
			library {
				cSources.from('srcs')
				publicHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "lib"
	}

	@Override
	protected List<String> allTasksForBuildAction(String variant) {
		return tasks.withOperatingSystemFamily(variant).allToLink
	}
}

class VisualStudioIdeCLibraryWithNativeTestSuiteFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-library'
				id 'dev.nokee.visual-studio-ide'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent library
				}
			}
		"""

		new CGreeterLib().writeToProject(testDirectory)
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterTest()
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return '''
			library {
				cSources.from('srcs')
				publicHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "lib-test"
	}

	@Override
	protected List<String> allTasksForBuildAction(String variant) {
		return [tasks.withOperatingSystemFamily(variant).compile] + tasks.withComponentName('test').withOperatingSystemFamily(variant).allToLink
	}
}

class VisualStudioIdeCLibraryWithStaticLinkageFunctionalTest extends VisualStudioIdeCLibraryFunctionalTest {
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
	protected List<String> allTasksForBuildAction(String variant) {
		return tasks.forStaticLibrary.withOperatingSystemFamily(variant).allToCreate
	}
}

class VisualStudioIdeCLibraryWithSharedLinkageFunctionalTest extends VisualStudioIdeCLibraryFunctionalTest {
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

class VisualStudioIdeCLibraryWithBothLinkageFunctionalTest extends VisualStudioIdeCLibraryFunctionalTest {
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
	protected List<String> allTasksForBuildAction(String variant) {
		return tasks.withOperatingSystemFamily(variant).withLinkage('shared').allToLink
	}
}
