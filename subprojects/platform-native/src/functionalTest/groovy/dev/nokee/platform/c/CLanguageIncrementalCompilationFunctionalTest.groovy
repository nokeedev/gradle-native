package dev.nokee.platform.c


import dev.nokee.fixtures.AbstractNativeLanguageIncrementalCompilationFunctionalTest
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.c.CTaskNames

class CApplicationNativeLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-application'
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'executable'
	}
}

class CLibraryNativeLanguageIncrementalCompilationFunctionalTest extends AbstractNativeLanguageIncrementalCompilationFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << '''
			plugins {
				id 'dev.nokee.c-library'
			}
		'''
	}

	@Override
	protected String getBinaryLifecycleTaskName() {
		return 'sharedLibrary'
	}
}

class CLibraryWithStaticLinkageNativeLanguageIncrementalCompilationFunctionalTest extends CLibraryNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << '''
			library {
				targetLinkages = [linkages.static]
			}
		'''
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

class CLibraryWithSharedLinkageNativeLanguageIncrementalCompilationFunctionalTest extends CLibraryNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << '''
			library {
				targetLinkages = [linkages.shared]
			}
		'''
	}
}

class CLibraryWithBothLinkageNativeLanguageIncrementalCompilationFunctionalTest extends CLibraryNativeLanguageIncrementalCompilationFunctionalTest {
	@Override
	protected void makeSingleProject() {
		super.makeSingleProject()
		buildFile << '''
			library {
				targetLinkages = [linkages.static, linkages.shared]
			}
		'''
	}

	@Override
	protected NativeProjectTasks getTaskNamesUnderTest() {
		return tasks.withLinkage('shared')
	}
}
