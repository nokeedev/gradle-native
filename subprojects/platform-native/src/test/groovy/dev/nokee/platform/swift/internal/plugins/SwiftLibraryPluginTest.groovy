package dev.nokee.platform.swift.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.swift.SwiftLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return SwiftLibraryExtension
	}
}

@Subject(SwiftLibraryPlugin)
class SwiftLibraryPluginTest extends AbstractPluginTest implements SwiftLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}
