package dev.nokee.testing.nativebase

import dev.gradleplugins.test.fixtures.sources.SourceElement
import dev.nokee.language.c.CTaskNames
import dev.nokee.platform.nativebase.ExecutableBinary
import dev.nokee.platform.nativebase.fixtures.CGreeterApp
import dev.nokee.platform.nativebase.fixtures.CGreeterLib

class CTestSuiteLibraryFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-library'
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
		return new CGreeterLib().withGenericTestSuite()
	}
}

class CTestSuiteApplicationFunctionalTest extends AbstractTestSuiteComponentFunctionalTest implements CTaskNames {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
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
		return new CGreeterApp().withGenericTestSuite()
	}
}
