package dev.nokee.platform.cpp.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.cpp.CppLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CppLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.cpp-library'
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return CppLibraryExtension
	}
}

@Subject(CppLibraryPlugin)
class CppLibraryPluginTest extends AbstractPluginTest implements CppLibraryPluginTestFixture {
}
