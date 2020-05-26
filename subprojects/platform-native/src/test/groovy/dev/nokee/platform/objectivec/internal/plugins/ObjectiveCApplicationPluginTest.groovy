package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.platform.objectivec.ObjectiveCApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-application'
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
		return ObjectiveCApplicationExtension
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'executable']
	}
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationPluginTest extends AbstractPluginTest implements ObjectiveCApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements ObjectiveCApplicationPluginTestFixture {
}

@Subject(ObjectiveCApplicationPlugin)
class ObjectiveCApplicationTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCApplicationPluginTestFixture {
}
