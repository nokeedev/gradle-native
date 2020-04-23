package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCGreeterLib

class JavaCJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return "compileMainSharedLibraryMainC"
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.c-language'
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter').withOptionalFeature()
	}
}
