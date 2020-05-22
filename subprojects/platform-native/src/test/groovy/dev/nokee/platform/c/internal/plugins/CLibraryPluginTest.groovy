package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.platform.c.CLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.c-library'
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
		return CLibraryExtension
	}
}

@Subject(CLibraryPlugin)
class CLibraryPluginTest extends AbstractPluginTest implements CLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CLibraryPlugin)
class CLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CLibraryPluginTestFixture {
}
