package dev.nokee.platform.cpp.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.cpp.CppApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CppApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.cpp-application'
	}

	void evaluateProject(String because) {
		projectUnderTest.evaluate()
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return CppApplicationExtension
	}
}

@Subject(CppApplicationPlugin)
class CppApplicationPluginTest extends AbstractPluginTest implements CppApplicationPluginTestFixture {
}
