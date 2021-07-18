package dev.nokee.platform.c

import dev.gradleplugins.fixtures.sources.SourceElement
import dev.nokee.fixtures.AbstractNativeApplicationComponentFunctionalTest
import dev.nokee.platform.nativebase.fixtures.CGreeterApp

class CApplicationComponentFunctionalTest extends AbstractNativeApplicationComponentFunctionalTest {
	@Override
	protected void makeSingleProject() {
		buildFile << """
			plugins {
				id 'dev.nokee.c-application'
			}
		"""
	}

	@Override
	protected SourceElement getComponentUnderTest() {
		return new CGreeterApp()
	}
}
