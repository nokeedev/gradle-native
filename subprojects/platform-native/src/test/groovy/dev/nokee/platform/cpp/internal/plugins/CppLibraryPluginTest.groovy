package dev.nokee.platform.cpp.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.platform.cpp.CppLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CppLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.cpp-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	String getExtensionNameUnderTest() {
		return 'library'
	}

	Class getExtensionType() {
		return CppLibraryExtension
	}
}

@Subject(CppLibraryPlugin)
class CppLibraryPluginTest extends AbstractPluginTest implements CppLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CppLibraryPlugin)
class CppLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CppLibraryPluginTestFixture {
}
