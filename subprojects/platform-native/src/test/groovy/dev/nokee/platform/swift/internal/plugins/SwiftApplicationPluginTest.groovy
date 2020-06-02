package dev.nokee.platform.swift.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTargetMachineAwarePluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.fixtures.AbstractVariantPluginTest
import dev.nokee.platform.nativebase.NativeApplication
import dev.nokee.platform.swift.SwiftApplicationExtension
import org.gradle.api.Project
import spock.lang.Subject

trait SwiftApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-application'
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
		return SwiftApplicationExtension
	}

	Class getVariantType() {
		return NativeApplication
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'executable']
	}
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationPluginTest extends AbstractPluginTest implements SwiftApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationTargetMachineAwarePluginTest extends AbstractTargetMachineAwarePluginTest implements SwiftApplicationPluginTestFixture {
}

@Subject(SwiftLibraryPlugin)
class SwiftApplicationTaskPluginTest extends AbstractTaskPluginTest implements SwiftApplicationPluginTestFixture {
}

@Subject(SwiftApplicationPlugin)
class SwiftApplicationVariantPluginTest extends AbstractVariantPluginTest implements SwiftApplicationPluginTestFixture {
}
