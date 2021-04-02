package dev.nokee.language.objectivecpp;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguagePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class ObjectiveCppLanguagePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.objective-cpp-language")
			.pluginClass(ObjectiveCppLanguagePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
