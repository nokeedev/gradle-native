package dev.nokee.platform.c

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.fixtures.CGreeterApp

class CApplicationNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
			}
		'''
	}

	@Override
	protected NativeSourceElement getComponentUnderTest() {
		return new CGreeterApp()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'executable'
	}
}

class CLibraryNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected NativeSourceElement getComponentUnderTest() {
		return new CGreeterApp()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

// TODO: explicit shared linkage
// TODO: explicit static linkage
// TODO: explicit both linkage
