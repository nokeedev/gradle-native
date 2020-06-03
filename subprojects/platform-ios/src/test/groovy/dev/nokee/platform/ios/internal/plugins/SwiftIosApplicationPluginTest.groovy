package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractPluginTest
import dev.nokee.fixtures.AbstractTaskPluginTest
import dev.nokee.platform.ios.SwiftIosApplicationExtension
import org.apache.commons.lang3.SystemUtils
import org.gradle.api.Project
import spock.lang.Requires
import spock.lang.Subject

trait SwiftIosApplicationPluginTestFixture {
	abstract Project getProjectUnderTest()

	String getPluginId() {
		return 'dev.nokee.swift-ios-application'
	}

	void applyPluginUnderTest() {
		projectUnderTest.apply plugin: pluginId
	}

	def getExtensionUnderTest() {
		return projectUnderTest.application
	}

	Class getExtensionType() {
		return SwiftIosApplicationExtension
	}

	String[] getExpectedVariantAwareTaskNames() {
		return ['objects', 'bundle']
	}
}

@Requires({ SystemUtils.IS_OS_MAC})
@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationPluginTest extends AbstractPluginTest implements SwiftIosApplicationPluginTestFixture {
	final String pluginIdUnderTest = pluginId
}

@Requires({SystemUtils.IS_OS_MAC})
@Subject(SwiftIosApplicationPlugin)
class SwiftIosApplicationTaskPluginTest extends AbstractTaskPluginTest implements SwiftIosApplicationPluginTestFixture {
}
