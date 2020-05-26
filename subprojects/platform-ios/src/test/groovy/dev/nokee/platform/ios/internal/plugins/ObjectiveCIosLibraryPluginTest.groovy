package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
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

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'staticLibrary']
	}
}

@Subject(ObjectiveCIosLibraryPlugin)
class ObjectiveCIosLibraryPluginTest extends AbstractPluginTest implements ObjectiveCIosLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(ObjectiveCIosLibraryPlugin)
class ObjectiveCIosLibraryTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCIosLibraryPluginTestFixture {
}
