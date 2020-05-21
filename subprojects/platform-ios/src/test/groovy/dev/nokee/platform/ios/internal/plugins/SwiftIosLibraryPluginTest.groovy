package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.ios.SwiftIosLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftIosLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-ios-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	Class getExtensionType() {
		return SwiftIosLibraryExtension
	}
}

@Subject(SwiftIosLibraryPlugin)
class SwiftIosLibraryPluginTest extends AbstractPluginTest implements SwiftIosLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}
