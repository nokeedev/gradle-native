package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.objective-c-library'
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
}
