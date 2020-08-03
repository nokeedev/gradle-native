package dev.nokee.ide.visualstudio

import dev.gradleplugins.test.fixtures.sources.SourceElement
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
				sources.from('srcs')
				privateHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "app"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
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
	protected List<String> getAllTasksForBuildAction() {
		return [tasks.compile] + tasks.withComponentName('test').allToLink + [tasks.withComponentName('test').relocateMainSymbol]
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
				sources.from('srcs')
				publicHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "lib"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
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
				sources.from('srcs')
				publicHeaders.from('hdrs')
			}
		'''
	}

	@Override
	protected String getVisualStudioProjectName() {
		return "lib-test"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return [tasks.compile] + tasks.withComponentName('test').allToLink
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
	protected List<String> getAllTasksForBuildAction() {
		return tasks.forStaticLibrary.allToCreate
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
	protected List<String> getAllTasksForBuildAction() {
		return tasks.withLinkage('shared').allToLink
	}
}
