package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.c.CApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: 'dev.nokee.c-application'
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return CApplicationExtension
	}
}

@Subject(CApplicationPlugin)
class CApplicationPluginTest extends AbstractPluginTest implements CApplicationPluginTestFixture {
}
