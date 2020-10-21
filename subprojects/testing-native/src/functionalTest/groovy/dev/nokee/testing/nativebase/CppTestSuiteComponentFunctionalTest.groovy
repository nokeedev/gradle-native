package dev.nokee.testing.nativebase

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.language.NativeProjectTasks
import dev.nokee.language.cpp.CppTaskNames
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp
import dev.nokee.platform.nativebase.fixtures.CppGreeterLib

class CppTestSuiteLibraryFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-library'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}
			import ${ExecutableBinary.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent library
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterLib().withGenericTestSuite()
	}

	@Override
	protected NativeProjectTasks getTasksUnderTest() {
		return tasks.withComponentName('test').forSharedLibrary
	}
}

class CppTestSuiteApplicationFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CppTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
				id 'dev.nokee.native-unit-testing'
			}

			import ${NativeTestSuite.canonicalName}
			import ${ExecutableBinary.canonicalName}

			testSuites {
				test(${NativeTestSuite.simpleName}) {
					testedComponent application
				}
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp().withGenericTestSuite()
	}
}
