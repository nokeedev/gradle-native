package dev.nokee.ide.visualstudio

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import org.junit.Assume

class VisualStudioIdeJavaNativeInterfaceLibraryFunctionalTest extends AbstractVisualStudioIdeNativeComponentPluginFunctionalTest implements CppTaskNames {
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
	protected String configureBuildTypes(String... buildTypes) {
		Assume.assumeFalse(true)
		return super.configureBuildTypes(buildTypes)
	}

	@Override
	protected String getVisualStudioProjectName() {
		return 'lib'
	}

	@Override
	protected List<String> getAllTasksForBuildAction() {
		return [':compileJava'] + tasks.allToLink
	}

	@Override
	protected List<String> getGeneratedHeaders() {
		return ['com_example_greeter_Greeter.h']
	}
}
