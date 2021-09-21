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
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib
import dev.nokee.platform.nativebase.fixtures.CppGreeterTest
import dev.nokee.testing.nativebase.NativeTestSuite
import org.junit.Assume

class VisualStudioIdeCppApplicationFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp()
	}

	@Override
	protected String configureCustomSourceLayout() {
		return '''
			application {
				cppSources.from('srcs')
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

class VisualStudioIdeCppApplicationWithNativeTestSuiteFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
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

		new CppGreeterApp().writeToProject(testDirectory)
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterTest()
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return '''
			application {
				cppSources.from('srcs')
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

class VisualStudioIdeCppLibraryFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeter().asLib()
	}

	@Override
	protected String configureCustomSourceLayout() {
		return '''
			library {
				cppSources.from('srcs')
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

class VisualStudioIdeCppLibraryWithNativeTestSuiteFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
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

		new CppGreeterLib().writeToProject(testDirectory)
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterTest()
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeTrue(false)
		return '''
			library {
				cppSources.from('srcs')
				privateHeaders.from('hdrs')
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


class VisualStudioIdeCppLibraryWithStaticLinkageFunctionalTest extends VisualStudioIdeCppLibraryFunctionalTest {
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

class VisualStudioIdeCppLibraryWithSharedLinkageFunctionalTest extends VisualStudioIdeCppLibraryFunctionalTest {
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

class VisualStudioIdeCppLibraryWithBothLinkageFunctionalTest extends VisualStudioIdeCppLibraryFunctionalTest {
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
		return tasks.withLinkage('shared').withOperatingSystemFamily(variant).allToLink
	}
}
