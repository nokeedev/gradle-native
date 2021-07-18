package dev.nokee.platform.cpp

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractTargetMachinesFunctionalTest
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

class CppApplicationTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements CppTaskNames {
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
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			executable("build/exes/main/${projectName}").exec().out == 'Bonjour, Alice!'
		} else {
			executable("build/exes/main/${variant}/${projectName}").exec().out == 'Bonjour, Alice!'
		}
	}
}

class CppLibraryTargetMachinesFunctionalTest extends AbstractTargetMachinesFunctionalTest implements CppTaskNames {
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
	protected String getTaskNameToAssembleDevelopmentBinary() {
		return 'assemble'
	}

	@Override
	protected void assertComponentUnderTestWasBuilt(String variant) {
		if (variant.isEmpty()) {
			sharedLibrary("build/libs/main/${projectName}").assertExists()
		} else {
			sharedLibrary("build/libs/main/${variant}/${projectName}").assertExists()
		}
	}
}
