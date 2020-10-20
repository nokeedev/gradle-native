package dev.nokee.platform.ios.internal.plugins

import dev.nokee.fixtures.AbstractComponentPluginTest
import dev.nokee.language.c.internal.CHeaderSetImpl
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetImpl
import dev.nokee.platform.ios.ObjectiveCIosApplicationExtension
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent
import org.gradle.api.Project

class ObjectiveCIosApplicationComponentPluginTest extends AbstractComponentPluginTest {
	@Override
	protected Class getExtensionTypeUnderTest() {
		return ObjectiveCIosApplicationExtension
	}

	@Override
	protected Class getComponentTypeUnderTest() {
		return DefaultIosApplicationComponent
	}

	@Override
	protected void applyPluginUnderTests(Project project) {
		project.apply plugin: 'dev.nokee.objective-c-ios-application'
	}

	@Override
	protected List<ExpectedLanguageSourceSet> getExpectedLanguageSourceSets() {
		return [newExpectedSourceSet('objectiveC', ObjectiveCSourceSetImpl).addConventionDirectory('src/main/objc'), newExpectedSourceSet('headers', CHeaderSetImpl, 'privateHeaders')]
	}
}
