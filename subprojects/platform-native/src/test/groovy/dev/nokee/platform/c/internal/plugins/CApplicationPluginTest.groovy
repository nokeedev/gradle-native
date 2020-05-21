package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.platform.c.CApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.c-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
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
	final String pluginIdUnderTest = pluginId
}
