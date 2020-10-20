package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractComponentPluginTest
import dev.nokee.language.swift.internal.SwiftSourceSetImpl
import dev.nokee.platform.ios.SwiftIosApplicationExtension
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent
import org.gradle.api.Project

class SwiftIosApplicationComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return SwiftIosApplicationExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultIosApplicationComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.swift-ios-application'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('swift', SwiftSourceSetImpl)]
	}
}
