package dev.nokee.language.base;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.base.internal.plugins.LanguageBasePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class LanguageBasePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.pluginClass(LanguageBasePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
