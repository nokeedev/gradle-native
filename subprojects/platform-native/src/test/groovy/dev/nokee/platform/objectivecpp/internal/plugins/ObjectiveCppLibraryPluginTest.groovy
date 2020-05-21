package dev.nokee.platform.objectivecpp.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.objectivecpp.ObjectiveCppLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCppLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.objective-cpp-library'
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return ObjectiveCppLibraryExtension
	}
}

@Subject(ObjectiveCppLibraryPlugin)
class ObjectiveCppLibraryPluginTest extends AbstractPluginTest implements ObjectiveCppLibraryPluginTestFixture {
}
