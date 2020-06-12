package dev.nokee.platform.cpp

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp

class CppApplicationNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-application'
			}
		'''
	}

	@Override
	protected NativeSourceElement getComponentUnderTest() {
		return new CppGreeterApp()
	}

	@Override
	protected String getExpectedCompilationFailureCause() {
		return 'C++ compiler failed while compiling broken.cpp'
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'executable'
	}
}

class CppLibraryNativeLanguageCompilationFunctionalTest extends AbstractNativeLanguageCompilationFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.cpp-library'
			}
		'''
	}

	@Override
	protected NativeSourceElement getComponentUnderTest() {
		return new CppGreeter()
	}

	@Override
	protected String getExpectedCompilationFailureCause() {
		return 'C++ compiler failed while compiling broken.cpp'
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

// TODO: explicit shared linkage
// TODO: explicit static linkage
// TODO: explicit both linkage
