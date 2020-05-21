package dev.nokee.platform.swift.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.swift.SwiftApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.swift-application'
	}

	void evaluateProject(String because) {
		projectUnderTest.evaluate()
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return SwiftApplicationExtension
	}
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationPluginTest extends AbstractPluginTest implements SwiftApplicationPluginTestFixture {
}
