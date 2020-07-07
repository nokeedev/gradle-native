package dev.nokee.platform.c

import dev.gradleplugins.test.fixtures.sources.NativeSourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.c.CTaskNames
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.jni.fixtures.CGreeter
import dev.nokee.platform.jni.fixtures.elements.CppGreeter
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

class CLibraryWithStaticLinkageNativeLanguageCompilationFunctionalTest extends CLibraryNativeLanguageCompilationFunctionalTest {
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
	protected String getBinaryLifecycleTaskName() {
		return 'staticLibrary'
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.forStaticLibrary
	}
}

class CLibraryWithSharedLinkageNativeLanguageCompilationFunctionalTest extends CLibraryNativeLanguageCompilationFunctionalTest {
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

class CLibraryWithBothLinkageNativeLanguageCompilationFunctionalTest extends CLibraryNativeLanguageCompilationFunctionalTest {
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
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
	}
}
