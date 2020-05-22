package dev.nokee.platform.cpp.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.platform.c.internal.plugins.CApplicationPlugin
import dev.nokee.platform.c.internal.plugins.CApplicationPluginTestFixture
import dev.nokee.platform.cpp.CppApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait CppApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.cpp-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	String getExtensionNameUnderTest() {
		return 'application'
	}

	Class getExtensionType() {
		return CppApplicationExtension
	}
}

@Subject(CppApplicationPlugin)
class CppApplicationPluginTest extends AbstractPluginTest implements CppApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CppApplicationPlugin)
class CppApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CppApplicationPluginTestFixture {
}
