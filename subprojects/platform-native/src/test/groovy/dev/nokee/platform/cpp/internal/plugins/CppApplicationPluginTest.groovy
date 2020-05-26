package dev.nokee.platform.cpp.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
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

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'executable']
	}
}

@Subject(CppApplicationPlugin)
class CppApplicationPluginTest extends AbstractPluginTest implements CppApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CppApplicationPlugin)
class CppApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CppApplicationPluginTestFixture {
}

@Subject(CppApplicationPlugin)
class CppApplicationTaskPluginTest extends AbstractTaskPluginTest implements CppApplicationPluginTestFixture {
}
