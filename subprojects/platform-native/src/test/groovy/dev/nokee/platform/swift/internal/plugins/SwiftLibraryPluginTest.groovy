package dev.nokee.platform.swift.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.swift.SwiftLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.swift-library'
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
}
