package dev.nokee.language.objectivec;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.objectivec.internal.plugins.ObjectiveCLanguagePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class ObjectiveCLanguagePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.objective-c-language")
			.pluginClass(ObjectiveCLanguagePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
