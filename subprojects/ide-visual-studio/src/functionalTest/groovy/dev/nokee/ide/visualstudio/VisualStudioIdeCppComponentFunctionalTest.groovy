package dev.nokee.ide.visualstudio

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp

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
	protected List<String> getAllTasksForBuildAction() {
		return tasks.forStaticLibrary.allToCreate
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
	protected List<String> getAllTasksForBuildAction() {
		return tasks.withLinkage('shared').allToLink
	}
}
