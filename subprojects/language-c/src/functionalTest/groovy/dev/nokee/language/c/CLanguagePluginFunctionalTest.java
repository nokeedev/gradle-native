package dev.nokee.language.c;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.c.internal.plugins.CLanguagePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class CLanguagePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.qualifiedPluginId("dev.nokee.c-language")
			.pluginClass(CLanguagePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
