package dev.nokee.platform.cpp

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeLanguageCompilationFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

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
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp()
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
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterLib()
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

class CppLibraryWithStaticLinkageNativeLanguageCompilationFunctionalTest extends CppLibraryNativeLanguageCompilationFunctionalTest {
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

class CppLibraryWithSharedLinkageNativeLanguageCompilationFunctionalTest extends CppLibraryNativeLanguageCompilationFunctionalTest {
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

class CppLibraryWithBothLinkageNativeLanguageCompilationFunctionalTest extends CppLibraryNativeLanguageCompilationFunctionalTest {
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
