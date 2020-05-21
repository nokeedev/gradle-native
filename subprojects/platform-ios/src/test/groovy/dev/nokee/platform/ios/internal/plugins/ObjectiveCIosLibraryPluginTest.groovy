package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.ios.ObjectiveCIosLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCIosLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-ios-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return ObjectiveCIosLibraryExtension
	}
}

@Subject(ObjectiveCIosLibraryPlugin)
class ObjectiveCIosLibraryPluginTest extends AbstractPluginTest implements ObjectiveCIosLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}
