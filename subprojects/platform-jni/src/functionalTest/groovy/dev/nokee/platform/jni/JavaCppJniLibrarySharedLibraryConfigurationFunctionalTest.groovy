package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCppGreeterLib

class JavaCppJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return 'compileMainSharedLibraryMainCpp'
	}

	@Override
	protected void makeSingleProject() {
		settingsFile << "rootProject.name = 'jni-greeter'"
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.cpp-language'
            }
        '''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCppGreeterLib('jni-greeter').withOptionalFeature()
	}
}
