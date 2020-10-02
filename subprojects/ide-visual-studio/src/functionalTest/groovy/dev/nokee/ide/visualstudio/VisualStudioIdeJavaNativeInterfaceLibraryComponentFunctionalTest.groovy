package dev.nokee.ide.visualstudio

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import org.junit.Assume

class VisualStudioIdeJavaNativeInterfaceLibraryComponentFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {

	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
				id 'dev.nokee.visual-studio-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('lib')
	}

	@Override
	protected String configureCustomSourceLayout() {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	@Override
	protected String configureBuildTypes() {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	@Override
	protected String getVisualStudioProjectName() {
		return 'lib'
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return [':compileJava'] + tasks.allToLink
	}

	protected SourceElement nativeSourceElementOf(SourceElement sourceElement) {
		return sourceElement.nativeSources
	}
}
