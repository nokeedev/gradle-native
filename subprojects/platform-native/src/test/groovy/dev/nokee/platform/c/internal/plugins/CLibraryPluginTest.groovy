package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.c.CLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.c-library'
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return CLibraryExtension
	}
}

@Subject(CLibraryPlugin)
class CLibraryPluginTest extends AbstractPluginTest implements CLibraryPluginTestFixture {
}
