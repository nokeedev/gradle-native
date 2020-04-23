package dev.nokee.platform.jni

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.platform.jni.fixtures.JavaJniObjectiveCGreeterLib

class JavaObjectiveCJniLibrarySharedLibraryConfigurationFunctionalTest extends AbstractSharedLibraryConfigurationFunctionalTest {
	@Override
	protected String getCompileTaskName() {
		return "compileMainSharedLibraryMainObjc"
	}

	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'java'
				id 'dev.nokee.jni-library'
				id 'dev.nokee.objective-c-language'
			}

			library.variants.configureEach {
				sharedLibrary.linkTask.configure {
					linkerArgs.add('-lobjc')
				}
			}
		'''
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new JavaJniObjectiveCGreeterLib().withOptionalFeature()
	}
}
