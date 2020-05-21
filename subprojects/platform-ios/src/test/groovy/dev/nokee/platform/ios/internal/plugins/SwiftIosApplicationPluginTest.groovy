package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.ios.SwiftIosApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftIosApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.swift-ios-application'
	}

	void evaluateProject(String because) {
		projectUnderTest.evaluate()
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return SwiftIosApplicationExtension
	}
}

@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationPluginTest extends AbstractPluginTest implements SwiftIosApplicationPluginTestFixture {
}
