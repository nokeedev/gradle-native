package dev.nokee.platform.c.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.fixtures.AbstractVariantPluginTest
import dev.nokee.platform.c.CApplicationExtension
import dev.nokee.platform.nativebase.NativeApplication
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

	String getExtensionNameUnderTest() {
		return 'application'
	}

	Class getExtensionType() {
		return CApplicationExtension
	}

	Class getVariantType() {
		return NativeApplication
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'executable']
	}
}

@Subject(CApplicationPlugin)
class CApplicationPluginTest extends AbstractPluginTest implements CApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(CApplicationPlugin)
class CApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationTaskPluginTest extends AbstractTaskPluginTest implements CApplicationPluginTestFixture {
}

@Subject(CApplicationPlugin)
class CApplicationVariantPluginTest extends AbstractVariantPluginTest implements CApplicationPluginTestFixture {
}
