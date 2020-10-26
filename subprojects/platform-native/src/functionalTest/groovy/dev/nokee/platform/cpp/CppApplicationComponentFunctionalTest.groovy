package dev.nokee.platform.cpp

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeApplicationComponentFunctionalTest
import dev.nokee.platform.nativebase.fixtures.CppGreeterApp

class CppApplicationComponentFunctionalTest extends AbstractNativeApplicationComponentFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.cpp-application'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CppGreeterApp()
	}
}
