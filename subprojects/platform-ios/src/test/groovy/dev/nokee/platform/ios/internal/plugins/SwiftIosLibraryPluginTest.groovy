package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.ios.SwiftIosLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftIosLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.swift-ios-library'
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
}
