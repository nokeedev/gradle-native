package dev.nokee.ide.xcode

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib
import org.junit.Assume
import spock.lang.Ignore

@Ignore
class XcodeIdeJavaNativeInterfaceLibraryFunctionalTest extends AbstractXcodeIdeNativeComponentPluginFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.cpp-language'
				id 'dev.nokee.xcode-ide'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('lib')
	}

	@Override
	protected String configureBuildTypes(String... buildTypes) {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	protected String configureCustomSourceLayout() {
		Assume.assumeFalse(true)
		throw new UnsupportedOperationException()
	}

	@Override
	protected String getProjectName() {
		return 'lib'
	}

	@Override
	protected List<String> getAllTasksToXcode() {
		return [':compileJava'] + super.allTasksToXcode
	}
}
