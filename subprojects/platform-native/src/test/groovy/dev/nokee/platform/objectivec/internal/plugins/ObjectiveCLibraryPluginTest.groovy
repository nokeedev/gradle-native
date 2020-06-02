package dev.nokee.platform.objectivec.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.fixtures.AbstractVariantPluginTest
import dev.nokee.platform.nativebase.NativeLibrary
import dev.nokee.platform.objectivec.ObjectiveCLibraryExtension
import org.gradle.api.Project
import spock.lang.Subject

trait ObjectiveCLibraryPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.objective-c-library'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.library
	}

	String getExtensionNameUnderTest() {
		return 'library'
	}

	Class getExtensionType() {
		return ObjectiveCLibraryExtension
	}

	Class getVariantType() {
		return NativeLibrary
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'sharedLibrary']
	}
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryPluginTest extends AbstractPluginTest implements ObjectiveCLibraryPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements ObjectiveCLibraryPluginTestFixture {
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryTaskPluginTest extends AbstractTaskPluginTest implements ObjectiveCLibraryPluginTestFixture {
}

@Subject(ObjectiveCLibraryPlugin)
class ObjectiveCLibraryVariantPluginTest extends AbstractVariantPluginTest implements ObjectiveCLibraryPluginTestFixture {
}
