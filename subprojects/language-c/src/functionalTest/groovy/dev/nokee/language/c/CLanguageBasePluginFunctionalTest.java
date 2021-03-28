package dev.nokee.language.c;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.Stream;

class CLanguageBasePluginFunctionalTest {
	@TestFactory
	Stream<DynamicTest> checkWellBehavedPlugin() {
		return new WellBehavedPluginTester()
			.pluginClass(CLanguageBasePlugin.class)
			.stream().map(TestCaseUtils::toJUnit5DynamicTest);
	}
}
