package dev.nokee.ide.visualstudio

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.ide.visualstudio.fixtures.VisualStudioIdeProjectFixture
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.platform.nativebase.fixtures.CGreeterApp

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
	protected String getProjectName() {
		return "app"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
	}
}

// TODO: Renable after fixing the tested component header file leak
//class VisualStudioIdeCApplicationWithNativeTestSuiteFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CTaskNames {
//	@Override
//	protected void makeSingleProject() {
//		buildFile << """
//			plugins {
//				id 'dev.nokee.c-application'
//				id 'dev.nokee.visual-studio-ide'
//				id 'dev.nokee.native-unit-testing'
//			}
//
//			import ${NativeTestSuite.canonicalName}
//
//			testSuites {
//				test(${NativeTestSuite.simpleName}) {
//					testedComponent application
//				}
//			}
//		"""
//		new CGreeterApp().writeToProject(testDirectory)
//	}
//
//	@Override
//	protected SourceElement getComponentUnderTest() {
//		return new CGreeterTest()
//	}
//
//	@Override
//	protected String configureCustomSourceLayout() {
//		Assume.assumeFalse(false)
//		return '''
//			application {
//				sources.from('srcs')
//				privateHeaders.from('hdrs')
//			}
//		'''
//	}
//
//	@Override
//	protected String getProjectName() {
//		return "app"
//	}
//
//	@Override
//	protected List<String> getAllTasksForBuildAction() {
//		return tasks.allToLink
//	}
//
//	@Override
//	protected VisualStudioIdeProjectFixture getVisualStudioProjectUnderTest() {
//		return visualStudioProject('test')
//	}
//}

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
		return new CGreeter().asLib()
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
	protected String getProjectName() {
		return "lib"
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return tasks.allToLink
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
