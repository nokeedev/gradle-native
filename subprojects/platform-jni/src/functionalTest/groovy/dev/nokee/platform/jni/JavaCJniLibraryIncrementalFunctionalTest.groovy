package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniCGreeterLib

class JavaCJniLibraryIncrementalFunctionalTest extends AbstractJavaJniLibraryIncrementalFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
            plugins {
                id 'java'
                id 'dev.nokee.jni-library'
                id 'dev.nokee.c-language'
            }
        '''
		settingsFile << "rootProject.name = 'jni-greeter'"
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniCGreeterLib('jni-greeter')
	}
}
