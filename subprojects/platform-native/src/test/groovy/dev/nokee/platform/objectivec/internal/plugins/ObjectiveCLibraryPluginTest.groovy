package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return ObjectiveCLibraryExtension
	}
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryPluginTest extends AbstractPluginTest implements ObjectiveCLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}
