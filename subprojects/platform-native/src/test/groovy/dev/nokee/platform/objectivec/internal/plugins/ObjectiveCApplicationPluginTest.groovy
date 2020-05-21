package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.objective-c-application'
	}

	void evaluateProject(String because) {
		projectUnderTest.evaluate()
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return ObjectiveCApplicationExtension
	}
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationPluginTest extends AbstractPluginTest implements ObjectiveCApplicationPluginTestFixture {
}
